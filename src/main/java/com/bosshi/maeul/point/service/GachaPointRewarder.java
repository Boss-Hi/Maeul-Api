package com.bosshi.maeul.point.service;

import com.bosshi.maeul.gacha.GachaPointsWonEvent;
import com.bosshi.maeul.point.type.PointReason;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/** 가챠에서 나온 포인트를 가챠와 같은 트랜잭션에서 적립한다. 가챠 결과마다 한 번만 적립한다 */
@Component
@RequiredArgsConstructor
public class GachaPointRewarder {

    private final PointService pointService;

    @EventListener
    public void onPointsWon(GachaPointsWonEvent event) {
        pointService.award(event.userId(), event.amount(), PointReason.GACHA, PointReason.GACHA.name() + ":" + event.gachaResultId());
    }
}
