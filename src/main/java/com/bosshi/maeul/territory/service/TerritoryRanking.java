package com.bosshi.maeul.territory.service;

import com.bosshi.maeul.territory.repository.dto.UserStay;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 점령 시간 순위. 시간이 같으면 같은 순위이고 다음 순위는 건너뛴다 (1, 1, 3).
 */
public final class TerritoryRanking {

    private TerritoryRanking() {
    }

    public record Ranked(int rank, Long userId, long seconds) {
    }

    public static List<Ranked> rank(List<UserStay> stays) {
        List<UserStay> sorted = stays.stream()
                .filter(stay -> stay.seconds() != null && stay.seconds() > 0)
                .sorted(Comparator.comparing(UserStay::seconds).reversed().thenComparing(UserStay::userId))
                .toList();
        List<Ranked> ranked = new ArrayList<>();
        for (int i = 0; i < sorted.size(); i++) {
            UserStay stay = sorted.get(i);
            int rank = i > 0 && sorted.get(i - 1).seconds().equals(stay.seconds()) ? ranked.get(i - 1).rank() : i + 1;
            ranked.add(new Ranked(rank, stay.userId(), stay.seconds()));
        }
        return ranked;
    }
}
