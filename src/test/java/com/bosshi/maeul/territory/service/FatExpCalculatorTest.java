package com.bosshi.maeul.territory.service;

import com.bosshi.maeul.territory.service.FatExpCalculator.Period;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("FatExpCalculator 테스트")
class FatExpCalculatorTest {

    private static final LocalDate NOV_19 = LocalDate.of(2026, 11, 19);
    private static final LocalDate NOV_20 = LocalDate.of(2026, 11, 20);
    private static final LocalDate NOV_22 = LocalDate.of(2026, 11, 22);

    @Test
    @DisplayName("2박 3일 일정 기간 안의 머문 시간과 방문일만 세고, 일정 밖 날짜는 세지 않는다")
    void countsOnlyInsidePeriod() {
        Map<LocalDate, Long> daily = Map.of(
                NOV_20, 3_600L, NOV_22, 600L,
                LocalDate.of(2026, 11, 23), 7_200L);

        long exp = FatExpCalculator.exp(NOV_19, 1_800L, daily, List.of(new Period(NOV_20, NOV_22)), 60);

        assertThat(exp).isEqualTo(70 + 2 * 60);
    }

    @Test
    @DisplayName("분양 당일이 일정 기간 안이면 분양 시각 이후 시간과 그날 방문일을 센다")
    void countsAdoptionDayInsidePeriod() {
        long exp = FatExpCalculator.exp(NOV_20, 1_200L, Map.of(), List.of(new Period(NOV_20, NOV_22)), 60);

        assertThat(exp).isEqualTo(20 + 60);
    }

    @Test
    @DisplayName("같은 시군구 일정이 겹쳐도 같은 날은 한 번만 센다")
    void overlappingPeriodsCountOnce() {
        long exp = FatExpCalculator.exp(NOV_19, 0L, Map.of(NOV_20, 600L),
                List.of(new Period(NOV_20, NOV_22), new Period(NOV_19, NOV_20)), 60);

        assertThat(exp).isEqualTo(10 + 60);
    }

    @Test
    @DisplayName("일정 기간이 없으면 머문 시간이 있어도 경험치는 0 이다")
    void noPeriodNoExp() {
        assertThat(FatExpCalculator.exp(NOV_19, 600L, Map.of(NOV_20, 3_600L), List.of(), 60)).isZero();
    }
}
