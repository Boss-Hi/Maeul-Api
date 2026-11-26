package com.bosshi.maeul.territory.service;

import com.bosshi.maeul.territory.entity.FatItineraryPeriod;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("일정 완료 묶음 테스트")
class ItineraryCompletionJudgeTest {

    @Test
    @DisplayName("같은 시군구에서 기간이 겹치거나 맞닿은 일정은 한 묶음, 떨어진 일정은 다른 묶음이다")
    void groupsOverlappingPeriods() {
        List<ItineraryCompletionJudge.Group> groups = ItineraryCompletionJudge.groups(List.of(
                period(3L, "2026-11-05", "2026-11-06"),
                period(1L, "2026-11-01", "2026-11-03"),
                period(2L, "2026-11-03", "2026-11-04"),
                period(4L, "2026-11-10", "2026-11-12")));

        assertThat(groups).hasSize(3);
        assertThat(groups.get(0).itineraryIds()).containsExactly(1L, 2L);
        assertThat(groups.get(0).endDate()).isEqualTo(LocalDate.parse("2026-11-04"));
        assertThat(groups.get(1).itineraryIds()).containsExactly(3L);
        assertThat(groups.get(2).itineraryIds()).containsExactly(4L);
    }

    @Test
    @DisplayName("안에 들어간 짧은 일정은 긴 일정의 종료일을 줄이지 않는다")
    void nestedPeriodKeepsLongerEnd() {
        List<ItineraryCompletionJudge.Group> groups = ItineraryCompletionJudge.groups(List.of(
                period(1L, "2026-11-01", "2026-11-10"),
                period(2L, "2026-11-02", "2026-11-03")));

        assertThat(groups).hasSize(1);
        assertThat(groups.getFirst().endDate()).isEqualTo(LocalDate.parse("2026-11-10"));
    }

    private static FatItineraryPeriod period(Long itineraryId, String start, String end) {
        return FatItineraryPeriod.builder().itineraryId(itineraryId).userId(7L).regionCode("11").subdivisionCode("560")
                .startDate(LocalDate.parse(start)).endDate(LocalDate.parse(end)).build();
    }
}
