package com.bosshi.maeul.territory.service;

import com.bosshi.maeul.territory.repository.dto.UserStay;
import com.bosshi.maeul.territory.service.TerritoryRanking.Ranked;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TerritoryRanking 테스트")
class TerritoryRankingTest {

    @Test
    @DisplayName("점령 시간 내림차순으로 순위를 매기고, 같은 시간은 같은 순위 후 다음 순위를 건너뛴다")
    void competitionRanking() {
        List<Ranked> ranked = TerritoryRanking.rank(List.of(
                new UserStay(1L, 600L), new UserStay(2L, 4200L), new UserStay(3L, 600L), new UserStay(4L, 300L)));

        assertThat(ranked).containsExactly(
                new Ranked(1, 2L, 4200), new Ranked(2, 1L, 600), new Ranked(2, 3L, 600), new Ranked(4, 4L, 300));
    }

    @Test
    @DisplayName("점령 시간이 0 인 사용자는 순위에서 뺀다")
    void excludesZero() {
        assertThat(TerritoryRanking.rank(List.of(new UserStay(1L, 0L), new UserStay(2L, 60L))))
                .containsExactly(new Ranked(1, 2L, 60));
    }
}
