package com.bosshi.maeul.territory.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.ToLongFunction;

/**
 * 점수 내림차순 순위. 같은 점수는 같은 순위이고 다음 순위는 건너뛴다 (1, 1, 3).
 */
public final class ScoreRanking {

    private ScoreRanking() {
    }

    public record Ranked<T>(int rank, T item) {
    }

    /**
     * @param tieOrder 같은 점수끼리의 표시 순서 (순위에는 영향 없음)
     */
    public static <T> List<Ranked<T>> rank(List<T> items, ToLongFunction<T> score, Comparator<T> tieOrder) {
        List<T> sorted = items.stream()
                .sorted(Comparator.comparingLong(score).reversed().thenComparing(tieOrder))
                .toList();
        List<Ranked<T>> ranked = new ArrayList<>();
        for (int i = 0; i < sorted.size(); i++) {
            boolean tied = i > 0 && score.applyAsLong(sorted.get(i)) == score.applyAsLong(sorted.get(i - 1));
            ranked.add(new Ranked<>(tied ? ranked.get(i - 1).rank() : i + 1, sorted.get(i)));
        }
        return ranked;
    }
}
