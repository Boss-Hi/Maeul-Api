package com.bosshi.maeul.gacha.service;

import com.bosshi.maeul.gacha.entity.GachaTicket;
import com.bosshi.maeul.gacha.entity.OfflinePrizeKind;
import com.bosshi.maeul.gacha.entity.PrizeQr;
import com.bosshi.maeul.gacha.entity.Store;
import com.bosshi.maeul.gacha.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@DisplayName("PrizeQrService 상품 QR 테스트")
class PrizeQrServiceTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 26, 12, 0);

    private final PrizeQrRepository prizeQrRepository = mock(PrizeQrRepository.class);
    private final OfflinePrizeKindRepository kindRepository = mock(OfflinePrizeKindRepository.class);
    private final GachaTicketRepository ticketRepository = mock(GachaTicketRepository.class);
    private final StoreService storeService = mock(StoreService.class);
    private final PrizeQrService service = new PrizeQrService(prizeQrRepository, kindRepository, ticketRepository,
            mock(OfflineGachaLogRepository.class), storeService, Clock.fixed(NOW.toInstant(ZoneOffset.UTC), ZoneOffset.UTC));

    @BeforeEach
    void setUp() {
        when(storeService.requireStoreOf(7L)).thenReturn(Store.builder().id(1L).name("가게").build());
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(GachaTicket.builder().id(10L).usedStoreId(1L).usedAt(NOW).build()));
        when(prizeQrRepository.findByCodeAndDeletedAtIsNull("GP-x")).thenReturn(Optional.of(PrizeQr.builder().id(20L).kindId(5L).code("GP-x").build()));
        when(prizeQrRepository.findById(20L)).thenReturn(Optional.of(PrizeQr.builder().id(20L).kindId(5L).code("GP-x").ticketId(10L).build()));
    }

    @Test
    @DisplayName("이 매장에서 사용된 가챠권과 아직 지급되지 않은 상품 QR 이면 연결하고 지급 완료로 바꾼다")
    void deliverLinksTicketAndPrize() {
        when(prizeQrRepository.markDelivered(20L, 1L, 7L, 10L, NOW)).thenReturn(1);

        PrizeQr delivered = service.deliver(7L, 10L, "GP-x");

        assertThat(delivered.getTicketId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("다른 매장에서 사용된 가챠권, 이미 상품이 연결된 가챠권, 상품 QR 이 아닌 코드는 거절한다")
    void deliverRejectsWrongStoreLinkedTicketOrCode() {
        when(ticketRepository.findById(11L)).thenReturn(Optional.of(GachaTicket.builder().id(11L).usedStoreId(2L).usedAt(NOW).build()));
        when(prizeQrRepository.existsByTicketId(10L)).thenReturn(true);

        assertThatThrownBy(() -> service.deliver(7L, 11L, "GP-x")).hasMessageContaining("이 매장에서 사용된");
        assertThatThrownBy(() -> service.deliver(7L, 10L, "GP-x")).hasMessageContaining("이미 상품이 연결");
        assertThatThrownBy(() -> service.deliver(7L, 10L, "GT-x")).hasMessageContaining("상품 QR 이 아닙니다");
        verify(prizeQrRepository, never()).markDelivered(anyLong(), anyLong(), anyLong(), anyLong(), any());
    }

    @Test
    @DisplayName("다른 매장 전용 상품 QR 은 지급할 수 없고, 이 매장 전용이거나 전용이 없으면 지급한다")
    void deliverRejectsPrizeKindOfAnotherStore() {
        when(kindRepository.findById(5L)).thenReturn(Optional.of(OfflinePrizeKind.builder().id(5L).name("아메리카노 1잔").storeId(2L).build()));

        assertThatThrownBy(() -> service.deliver(7L, 10L, "GP-x")).hasMessageContaining("지급할 수 없는 상품 QR");
        verify(prizeQrRepository, never()).markDelivered(anyLong(), anyLong(), anyLong(), anyLong(), any());

        when(prizeQrRepository.markDelivered(20L, 1L, 7L, 10L, NOW)).thenReturn(1);
        when(kindRepository.findById(5L)).thenReturn(Optional.of(OfflinePrizeKind.builder().id(5L).name("아메리카노 1잔").storeId(1L).build()));
        assertThat(service.deliver(7L, 10L, "GP-x").getTicketId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("이미 지급된 상품 QR 은 다시 지급할 수 없다")
    void deliverRejectsAlreadyDeliveredQr() {
        when(prizeQrRepository.markDelivered(20L, 1L, 7L, 10L, NOW)).thenReturn(0);

        assertThatThrownBy(() -> service.deliver(7L, 10L, "GP-x")).hasMessageContaining("이미 지급된");
    }

    @Test
    @DisplayName("지급 취소는 지급된 상품 QR 에만, 사유와 함께 할 수 있다")
    void cancelDeliveryRequiresDeliveredQrAndReason() {
        PrizeQr delivered = PrizeQr.builder().id(21L).deliveredAt(NOW).ticketId(10L).deliveredStoreId(1L).build();
        when(prizeQrRepository.findById(21L)).thenReturn(Optional.of(delivered));
        when(prizeQrRepository.clearDelivery(21L)).thenReturn(1);

        assertThatThrownBy(() -> service.cancelDelivery(21L, 99L, "")).hasMessageContaining("사유");
        assertThatThrownBy(() -> service.cancelDelivery(20L, 99L, "오조작")).hasMessageContaining("지급되지 않은");

        service.cancelDelivery(21L, 99L, "오조작");

        verify(prizeQrRepository).clearDelivery(21L);
    }

    @Test
    @DisplayName("발급한 상품 QR 의 코드는 서로 다르고 상품 QR 접두사를 가진다")
    void issueCreatesUniquePrizeCodes() {
        when(kindRepository.existsById(5L)).thenReturn(true);
        when(prizeQrRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var qrs = service.issue(5L, 200);

        assertThat(qrs).allSatisfy(qr -> assertThat(qr.getCode()).startsWith("GP-"));
        assertThat(qrs.stream().map(PrizeQr::getCode).collect(Collectors.toSet())).hasSize(200);
        assertThatThrownBy(() -> service.issue(5L, 0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.issue(5L, 1001)).isInstanceOf(IllegalArgumentException.class);
    }
}
