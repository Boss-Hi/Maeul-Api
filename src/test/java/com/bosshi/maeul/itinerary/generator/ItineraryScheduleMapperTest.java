package com.bosshi.maeul.itinerary.generator;

import com.bosshi.maeul.itinerary.entity.ItineraryDay;
import com.bosshi.maeul.itinerary.entity.ItinerarySchedule;
import com.bosshi.maeul.itinerary.generator.impl.ItineraryGeneratorImpl.ItineraryTourJson;
import com.bosshi.maeul.itinerary.type.ItineraryScheduleType;
import com.bosshi.maeul.openapi.entity.Tour;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ItineraryScheduleMapper 테스트")
class ItineraryScheduleMapperTest {

    private final ItineraryDay day = ItineraryDay.builder().build();
    private final Tour known = Tour.builder().contentId("123").title("축제").build();
    private final Map<String, Tour> knownTours = Map.of("123", known);

    @Test
    @DisplayName("알려진 contentId 는 TOUR 일정이 된다")
    void knownContentIdBecomesTour() {
        ItinerarySchedule schedule = map(new ItineraryTourJson("123", 1, null, null)).orElseThrow();

        assertThat(schedule.getType()).isEqualTo(ItineraryScheduleType.TOUR.name());
        assertThat(schedule.getTour()).isSameAs(known);
        assertThat(schedule.getSequence()).isEqualTo(1);
    }

    @Test
    @DisplayName("contentId 가 없으면 title/description 으로 NORMAL 일정이 된다")
    void missingContentIdBecomesNormal() {
        ItinerarySchedule schedule = map(new ItineraryTourJson(null, 2, "점심 식사", "시장 국밥")).orElseThrow();

        assertThat(schedule.getType()).isEqualTo(ItineraryScheduleType.NORMAL.name());
        assertThat(schedule.getTour()).isNull();
        assertThat(schedule.getTitle()).isEqualTo("점심 식사");
        assertThat(schedule.getDescription()).isEqualTo("시장 국밥");
    }

    @Test
    @DisplayName("알 수 없는 contentId 라도 title 이 있으면 NORMAL 일정으로 저장한다")
    void unknownContentIdWithTitleBecomesNormal() {
        ItinerarySchedule schedule = map(new ItineraryTourJson("FESTIVAL_GYEONGSAN_CAFE", 3, "경산 카페", null)).orElseThrow();

        assertThat(schedule.getType()).isEqualTo(ItineraryScheduleType.NORMAL.name());
        assertThat(schedule.getTour()).isNull();
        assertThat(schedule.getTitle()).isEqualTo("경산 카페");
    }

    @Test
    @DisplayName("contentId 도 title 도 쓸 수 없으면 제외한다")
    void nothingUsableIsSkipped() {
        assertThat(map(new ItineraryTourJson("UNKNOWN", 4, null, null))).isEmpty();
        assertThat(map(new ItineraryTourJson(null, 5, " ", null))).isEmpty();
    }

    private Optional<ItinerarySchedule> map(ItineraryTourJson item) {
        return ItineraryScheduleMapper.toSchedule(day, item, knownTours::get);
    }
}
