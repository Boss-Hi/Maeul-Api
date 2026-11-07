package com.bosshi.maeul.territory.service;

import com.bosshi.maeul.territory.service.OccupationCalculator.RegionPoint;
import com.bosshi.maeul.territory.service.OccupationCalculator.Visit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OccupationCalculator 테스트")
class OccupationCalculatorTest {

    private static final LocalDateTime BASE = LocalDateTime.of(2026, 11, 6, 9, 0);
    private static final Duration MAX_GAP = Duration.ofMinutes(3);
    private static final String YEONGDEUNGPO = "11560", MAPO = "11440";

    @Test
    @DisplayName("같은 구에 연속으로 있던 간격만 그 구의 점령 시간이 되고, 구가 바뀌는 간격은 세지 않는다")
    void countsConsecutiveIntervalsInSameRegion() {
        List<RegionPoint> points = new ArrayList<>();
        for (int m = 0; m <= 40; m++) points.add(at(m, YEONGDEUNGPO)); // 40분
        for (int m = 41; m <= 51; m++) points.add(at(m, MAPO));        // 10분 (40->41 은 경계)

        assertThat(OccupationCalculator.staySeconds(points, MAX_GAP))
                .containsExactlyInAnyOrderEntriesOf(Map.of(YEONGDEUNGPO, 2400L, MAPO, 600L));
    }

    @Test
    @DisplayName("기록이 maxGap 보다 오래 끊긴 간격과 지역을 모르는 기록이 낀 간격은 세지 않는다")
    void skipsLongGapsAndUnknownRegions() {
        List<RegionPoint> points = List.of(
                at(0, YEONGDEUNGPO), at(1, YEONGDEUNGPO),
                at(4, YEONGDEUNGPO),        // 3분 간격: maxGap 이하라 셈
                at(10, YEONGDEUNGPO),       // 6분 끊김: 안 셈
                at(11, null), at(12, YEONGDEUNGPO), at(13, YEONGDEUNGPO));

        assertThat(OccupationCalculator.staySeconds(points, MAX_GAP)).containsExactlyEntriesOf(Map.of(YEONGDEUNGPO, 60L + 180L + 60L));
    }

    @Test
    @DisplayName("현재 방문은 마지막 기록의 구에 연속으로 머문 구간이고, 마지막 기록이 maxGap 이내면 live")
    void currentVisitFromLastRun() {
        List<RegionPoint> points = List.of(at(0, YEONGDEUNGPO), at(1, MAPO), at(2, MAPO), at(5, MAPO));

        Visit live = OccupationCalculator.currentVisit(points, MAX_GAP, BASE.plusMinutes(7)).orElseThrow();
        assertThat(live).isEqualTo(new Visit(MAPO, BASE.plusMinutes(1), BASE.plusMinutes(5), 240, true));

        assertThat(OccupationCalculator.currentVisit(points, MAX_GAP, BASE.plusMinutes(9)).orElseThrow().live()).isFalse();
    }

    @Test
    @DisplayName("기록이 없거나 마지막 기록의 지역을 모르면 현재 방문이 없다")
    void noCurrentVisit() {
        assertThat(OccupationCalculator.currentVisit(List.of(), MAX_GAP, BASE)).isEmpty();
        assertThat(OccupationCalculator.currentVisit(List.of(at(0, MAPO), at(1, null)), MAX_GAP, BASE)).isEmpty();
    }

    private static RegionPoint at(int minutes, String regionId) {
        return new RegionPoint(BASE.plusMinutes(minutes), regionId);
    }
}
