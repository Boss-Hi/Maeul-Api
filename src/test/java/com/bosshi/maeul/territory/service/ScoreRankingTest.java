package com.bosshi.maeul.territory.service;

import com.bosshi.maeul.territory.service.ScoreRanking.Ranked;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ScoreRanking 테스트")
class ScoreRankingTest {

    @Test
    @DisplayName("점수 내림차순으로 순위를 매기고, 같은 점수는 같은 순위 후 다음 순위를 건너뛴다")
    void competitionRanking() {
        Map<String, Long> scores = Map.of("a", 10L, "b", 30L, "c", 10L, "d", 0L);

        List<Ranked<String>> ranked = ScoreRanking.rank(List.of("a", "b", "c", "d"), scores::get, Comparator.naturalOrder());

        assertThat(ranked).containsExactly(new Ranked<>(1, "b"), new Ranked<>(2, "a"), new Ranked<>(2, "c"), new Ranked<>(4, "d"));
    }
}
