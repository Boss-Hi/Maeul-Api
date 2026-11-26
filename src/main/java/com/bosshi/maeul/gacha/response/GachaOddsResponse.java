package com.bosshi.maeul.gacha.response;

import com.bosshi.maeul.gacha.GachaType;
import com.bosshi.maeul.gacha.RewardType;
import com.bosshi.maeul.gacha.dto.GachaOdds;

import java.util.List;

/** 가챠의 지금 확률 (0 ~ 1). 재고가 없는 상품은 빠진다 */
public record GachaOddsResponse(GachaType gachaType, List<Entry> entries) {

    public static GachaOddsResponse from(GachaOdds odds) {
        return new GachaOddsResponse(odds.gachaType(), odds.entries().stream()
                .map(entry -> new Entry(entry.label(), entry.rewardType(), entry.amount(), entry.probability()))
                .toList());
    }

    public record Entry(String label, RewardType rewardType, Long amount, double probability) {
    }
}
