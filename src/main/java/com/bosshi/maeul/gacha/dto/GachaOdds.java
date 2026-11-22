package com.bosshi.maeul.gacha.dto;

import com.bosshi.maeul.gacha.GachaType;
import com.bosshi.maeul.gacha.RewardType;

import java.util.List;

/** 가챠의 지금 확률. 재고가 없는 상품은 빠진다 */
public record GachaOdds(GachaType gachaType, List<Entry> entries) {

    public record Entry(String label, RewardType rewardType, Long amount, double probability) {
    }
}
