package com.bosshi.maeul.gacha.service;

import com.bosshi.maeul.gacha.entity.GachaOutcome;

import java.util.List;
import java.util.Optional;
import java.util.random.RandomGenerator;

/** 가중치로 결과 하나를 고른다. 가중치가 0 이하인 결과는 고르지 않는다 */
final class GachaPicker {

    private GachaPicker() {
    }

    static Optional<GachaOutcome> pick(List<GachaOutcome> candidates, RandomGenerator random) {
        long total = candidates.stream().mapToLong(GachaPicker::weight).sum();
        if (total <= 0) {
            return Optional.empty();
        }
        long roll = random.nextLong(total);
        for (GachaOutcome outcome : candidates) {
            roll -= weight(outcome);
            if (roll < 0) {
                return Optional.of(outcome);
            }
        }
        throw new IllegalStateException("가중치 합 안에서 결과를 고르지 못했습니다.");
    }

    static long weight(GachaOutcome outcome) {
        return outcome.getWeight() == null ? 0 : Math.max(0, outcome.getWeight());
    }
}
