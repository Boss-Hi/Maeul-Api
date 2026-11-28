package com.bosshi.maeul.gacha.service;

import com.bosshi.maeul.gacha.GachaTicketIssuedEvent;
import com.bosshi.maeul.gacha.TicketEventType;
import com.bosshi.maeul.gacha.dto.TicketView;
import com.bosshi.maeul.gacha.entity.GachaTicket;
import com.bosshi.maeul.gacha.entity.GachaTicketRule;
import com.bosshi.maeul.gacha.entity.Store;
import com.bosshi.maeul.gacha.repository.*;
import com.bosshi.maeul.gacha.type.TicketStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@DisplayName("GachaTicketService 가챠권 테스트")
class GachaTicketServiceTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 26, 12, 0);

    private final GachaTicketRepository ticketRepository = mock(GachaTicketRepository.class);
    private final GachaTicketRuleRepository ruleRepository = mock(GachaTicketRuleRepository.class);
    private final PrizeQrRepository prizeQrRepository = mock(PrizeQrRepository.class);
    private final StoreService storeService = mock(StoreService.class);
    private final ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
    private final GachaTicketService service = new GachaTicketService(ticketRepository, ruleRepository, prizeQrRepository,
            mock(OfflinePrizeKindRepository.class), mock(OfflineGachaLogRepository.class), storeService, eventPublisher,
            Clock.fixed(NOW.toInstant(ZoneOffset.UTC), ZoneOffset.UTC));

    @BeforeEach
    void setUp() {
        when(storeService.requireStoreOf(7L)).thenReturn(Store.builder().id(1L).name("가게").build());
    }

    private static GachaTicket ticket(LocalDateTime expiresAt, LocalDateTime usedAt) {
        return GachaTicket.builder().id(10L).userId(3L).code("GT-abc").expiresAt(expiresAt).usedAt(usedAt).build();
    }

    @Test
    @DisplayName("발급은 설정의 유효기간만큼 뒤에 만료되는 가챠권을 만든다")
    void issueSetsExpiryFromRule() {
        when(ruleRepository.findById(TicketEventType.ITINERARY_COMPLETION))
                .thenReturn(Optional.of(GachaTicketRule.builder().enabled(true).validityDays(30).build()));
        when(ticketRepository.existsByUserIdAndEventTypeAndRefKey(any(), any(), any())).thenReturn(false);

        service.issue(3L, TicketEventType.ITINERARY_COMPLETION, "itinerary:1");

        ArgumentCaptor<GachaTicket> saved = ArgumentCaptor.forClass(GachaTicket.class);
        verify(ticketRepository).save(saved.capture());
        assertThat(saved.getValue().getExpiresAt()).isEqualTo(NOW.plusDays(30));
        assertThat(saved.getValue().getCode()).startsWith("GT-");
        verify(eventPublisher).publishEvent(new GachaTicketIssuedEvent(3L, null, TicketEventType.ITINERARY_COMPLETION, NOW.plusDays(30)));
    }

    @Test
    @DisplayName("발급이 꺼져 있거나 같은 일로 이미 발급했으면 만들지 않는다")
    void issueSkipsWhenDisabledOrDuplicate() {
        when(ruleRepository.findById(TicketEventType.ITINERARY_COMPLETION))
                .thenReturn(Optional.of(GachaTicketRule.builder().enabled(false).validityDays(30).build()));
        service.issue(3L, TicketEventType.ITINERARY_COMPLETION, "itinerary:1");

        when(ruleRepository.findById(TicketEventType.ITINERARY_COMPLETION))
                .thenReturn(Optional.of(GachaTicketRule.builder().enabled(true).validityDays(30).build()));
        when(ticketRepository.existsByUserIdAndEventTypeAndRefKey(3L, TicketEventType.ITINERARY_COMPLETION, "itinerary:1")).thenReturn(true);
        service.issue(3L, TicketEventType.ITINERARY_COMPLETION, "itinerary:1");

        verify(ticketRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any(Object.class));
    }

    @Test
    @DisplayName("스캔은 사용 가능한 가챠권을 한 번만 사용됨으로 바꾼다")
    void useMarksTicketUsedOnce() {
        GachaTicket usable = ticket(NOW.plusDays(1), null);
        when(ticketRepository.findByCodeAndDeletedAtIsNull("GT-abc")).thenReturn(Optional.of(usable));
        when(ticketRepository.markUsed(10L, 1L, 7L, NOW)).thenReturn(1);
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(usable));

        assertThat(service.use(7L, "GT-abc")).isSameAs(usable);

        when(ticketRepository.markUsed(10L, 1L, 7L, NOW)).thenReturn(0);
        assertThatThrownBy(() -> service.use(7L, "GT-abc")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("만료됐거나 이미 사용됐거나 가챠권 QR 이 아니면 스캔할 수 없다")
    void useRejectsExpiredUsedOrWrongCode() {
        when(ticketRepository.findByCodeAndDeletedAtIsNull("GT-expired")).thenReturn(Optional.of(ticket(NOW.minusSeconds(1), null)));
        when(ticketRepository.findByCodeAndDeletedAtIsNull("GT-used")).thenReturn(Optional.of(ticket(NOW.plusDays(1), NOW.minusHours(1))));

        assertThatThrownBy(() -> service.use(7L, "GT-expired")).hasMessageContaining("유효기간");
        assertThatThrownBy(() -> service.use(7L, "GT-used")).hasMessageContaining("이미 사용");
        assertThatThrownBy(() -> service.use(7L, "GP-prize")).hasMessageContaining("가챠권 QR");
        verify(ticketRepository, never()).markUsed(anyLong(), anyLong(), anyLong(), any());
    }

    @Test
    @DisplayName("상품이 연결된 가챠권은 사용 취소할 수 없고, 사유 없이도 취소할 수 없다")
    void cancelUseBlockedByLinkedPrizeOrMissingReason() {
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket(NOW.plusDays(1), NOW.minusHours(1))));
        when(prizeQrRepository.existsByTicketId(10L)).thenReturn(true);

        assertThatThrownBy(() -> service.cancelUse(10L, 99L, "오조작")).hasMessageContaining("상품 QR 지급을 먼저");
        assertThatThrownBy(() -> service.cancelUse(10L, 99L, " ")).hasMessageContaining("사유");
        verify(ticketRepository, never()).clearUse(anyLong());
    }

    @Test
    @DisplayName("사용을 취소하면 사용 전으로 돌아가고 이력이 남는다")
    void cancelUseResetsTicket() {
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket(NOW.plusDays(1), NOW.minusHours(1))));
        when(prizeQrRepository.existsByTicketId(10L)).thenReturn(false);
        when(ticketRepository.clearUse(10L)).thenReturn(1);

        service.cancelUse(10L, 99L, "장비 고장");

        verify(ticketRepository).clearUse(10L);
    }

    @Test
    @DisplayName("상태: 사용 가능, 사용됨, 상품 받음, 만료 (사용된 권은 만료되지 않는다)")
    void statusFollowsUsageAndExpiry() {
        assertThat(GachaTicketService.statusOf(ticket(NOW.plusDays(1), null), false, NOW)).isEqualTo(TicketStatus.USABLE);
        assertThat(GachaTicketService.statusOf(ticket(NOW.minusDays(1), null), false, NOW)).isEqualTo(TicketStatus.EXPIRED);
        assertThat(GachaTicketService.statusOf(ticket(NOW.minusDays(1), NOW.minusDays(2)), false, NOW)).isEqualTo(TicketStatus.USED);
        assertThat(GachaTicketService.statusOf(ticket(NOW.plusDays(1), NOW.minusHours(1)), true, NOW)).isEqualTo(TicketStatus.PRIZE_RECEIVED);
    }

    @Test
    @DisplayName("유효기간은 1일 이상이어야 한다")
    void updateRuleRejectsZeroDays() {
        assertThatThrownBy(() -> service.updateRule(TicketEventType.ITINERARY_COMPLETION, true, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
