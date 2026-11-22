package com.bosshi.maeul.gacha.service;

import com.bosshi.maeul.gacha.GachaDraw;
import com.bosshi.maeul.gacha.GachaPointsWonEvent;
import com.bosshi.maeul.gacha.GachaType;
import com.bosshi.maeul.gacha.RewardType;
import com.bosshi.maeul.gacha.entity.*;
import com.bosshi.maeul.gacha.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.*;

@DisplayName("GachaService 뽑기 테스트")
class GachaServiceTest {

    private final GachaOutcomeRepository outcomeRepository = mock(GachaOutcomeRepository.class);
    private final GachaResultRepository resultRepository = mock(GachaResultRepository.class);
    private final PrizeRepository prizeRepository = mock(PrizeRepository.class);
    private final PrizeVoucherRepository voucherRepository = mock(PrizeVoucherRepository.class);
    private final UserItemRepository userItemRepository = mock(UserItemRepository.class);
    private final ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
    private final GachaService service = new GachaService(outcomeRepository, resultRepository, prizeRepository,
            mock(ItemRepository.class), voucherRepository, userItemRepository, eventPublisher, new Random(1));

    private static final GachaOutcome PRIZE = GachaOutcome.builder().id(1L).gachaType(GachaType.GRADUATION)
            .rewardType(RewardType.PRIZE).label("상품").weight(1).prizeId(10L).build();
    private static final GachaOutcome POINT = GachaOutcome.builder().id(2L).gachaType(GachaType.GRADUATION)
            .rewardType(RewardType.POINT).label("500 포인트").weight(1).amount(500L).build();

    @BeforeEach
    void setUp() {
        when(resultRepository.findByUserIdAndGachaTypeAndRefKey(any(), any(), any())).thenReturn(Optional.empty());
        when(resultRepository.saveAndFlush(any())).thenAnswer(invocation -> {
            GachaResult result = invocation.getArgument(0);
            result.setId(99L);
            return result;
        });
        when(voucherRepository.save(any())).thenAnswer(invocation -> {
            PrizeVoucher voucher = invocation.getArgument(0);
            voucher.setId(5L);
            return voucher;
        });
    }

    @Test
    @DisplayName("재고가 없는 상품은 빼고 남은 결과에서 뽑는다")
    void skipsOutOfStockPrize() {
        when(outcomeRepository.findByGachaTypeAndDeletedAtIsNullOrderByIdAsc(GachaType.GRADUATION)).thenReturn(List.of(PRIZE, POINT));
        when(prizeRepository.findByIdInAndDeletedAtIsNull(Set.of(10L))).thenReturn(List.of(Prize.builder().id(10L).stock(0).build()));

        for (int i = 0; i < 20; i++) {
            assertThat(service.draw(7L, GachaType.GRADUATION, "fat:" + i).rewardType()).isEqualTo(RewardType.POINT);
        }
        verify(prizeRepository, never()).takeOne(any());
        verify(eventPublisher, times(20)).publishEvent(any(GachaPointsWonEvent.class));
    }

    @Test
    @DisplayName("재고를 줄이지 못한 상품(동시에 소진)은 다시 뽑고, 상품이 나오면 교환권을 발급한다")
    void redrawsWhenStockRunsOutAndIssuesVoucher() {
        GachaOutcome onlyPrize = GachaOutcome.builder().id(1L).gachaType(GachaType.GRADUATION)
                .rewardType(RewardType.PRIZE).label("상품").weight(1).prizeId(10L).build();
        when(outcomeRepository.findByGachaTypeAndDeletedAtIsNullOrderByIdAsc(GachaType.GRADUATION)).thenReturn(List.of(onlyPrize, POINT));
        when(prizeRepository.findByIdInAndDeletedAtIsNull(anyCollection())).thenReturn(List.of(Prize.builder().id(10L).stock(1).build()));
        when(prizeRepository.takeOne(10L)).thenReturn(1);

        GachaDraw prizeDraw = null;
        for (int i = 0; i < 20 && prizeDraw == null; i++) {
            GachaDraw draw = service.draw(7L, GachaType.GRADUATION, "fat:" + i);
            if (draw.rewardType() == RewardType.PRIZE) {
                prizeDraw = draw;
            }
        }
        assertThat(prizeDraw).isNotNull();
        assertThat(prizeDraw.voucherId()).isEqualTo(5L);

        when(prizeRepository.takeOne(10L)).thenReturn(0);
        for (int i = 0; i < 20; i++) {
            assertThat(service.draw(7L, GachaType.GRADUATION, "again:" + i).rewardType()).isEqualTo(RewardType.POINT);
        }
    }

    @Test
    @DisplayName("같은 일(refKey)로 이미 뽑았으면 다시 뽑지 않고 지급도 하지 않는다")
    void sameRefKeyDrawsOnce() {
        GachaResult existing = GachaResult.builder().id(3L).userId(7L).gachaType(GachaType.GRADUATION).refKey("fat:1")
                .outcomeId(2L).rewardType(RewardType.POINT).label("500 포인트").amount(500L).build();
        when(resultRepository.findByUserIdAndGachaTypeAndRefKey(7L, GachaType.GRADUATION, "fat:1")).thenReturn(Optional.of(existing));

        assertThat(service.draw(7L, GachaType.GRADUATION, "fat:1").resultId()).isEqualTo(3L);
        verify(resultRepository, never()).saveAndFlush(any());
        verifyNoInteractions(eventPublisher);
    }

    @Test
    @DisplayName("같은 아이템이 또 나오면 수량이 는다")
    void itemQuantityGrows() {
        GachaOutcome item = GachaOutcome.builder().id(4L).gachaType(GachaType.ITINERARY_COMPLETION)
                .rewardType(RewardType.ITEM).label("아이템").weight(1).itemId(20L).build();
        when(outcomeRepository.findByGachaTypeAndDeletedAtIsNullOrderByIdAsc(GachaType.ITINERARY_COMPLETION)).thenReturn(List.of(item));
        UserItem owned = UserItem.builder().userId(7L).itemId(20L).quantity(1).build();
        when(userItemRepository.findByUserIdAndItemId(7L, 20L)).thenReturn(Optional.of(owned));

        service.draw(7L, GachaType.ITINERARY_COMPLETION, "itinerary:1");

        assertThat(owned.getQuantity()).isEqualTo(2);
        verify(userItemRepository).save(owned);
    }

    @Test
    @DisplayName("뽑을 수 있는 결과가 없으면 실패한다")
    void failsWithoutOutcomes() {
        when(outcomeRepository.findByGachaTypeAndDeletedAtIsNullOrderByIdAsc(GachaType.MISSION)).thenReturn(List.of());

        assertThatThrownBy(() -> service.draw(7L, GachaType.MISSION, "mission:1")).isInstanceOf(IllegalStateException.class);
    }
}
