package com.bosshi.maeul.itinerary.generator;

import com.bosshi.maeul.itinerary.entity.ItineraryDay;
import com.bosshi.maeul.itinerary.entity.ItinerarySchedule;
import com.bosshi.maeul.itinerary.generator.impl.ItineraryGeneratorImpl.ItineraryTourJson;
import com.bosshi.maeul.itinerary.type.ItineraryScheduleType;
import com.bosshi.maeul.openapi.entity.Tour;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.function.Function;

/**
 * 생성기 응답 항목을 상세 일정으로 변환한다.
 * <ul>
 *   <li>contentId 가 알려진 관광지면 TOUR 일정</li>
 *   <li>contentId 가 없거나 알 수 없는 값이면 title/description 으로 NORMAL 일정</li>
 *   <li>둘 다 없으면 제외</li>
 * </ul>
 */
@Slf4j
final class ItineraryScheduleMapper {

    private ItineraryScheduleMapper() {
    }

    static Optional<ItinerarySchedule> toSchedule(ItineraryDay day, ItineraryTourJson item, Function<String, Tour> tourResolver) {
        Tour tour = StringUtils.hasText(item.contentId()) ? tourResolver.apply(item.contentId()) : null;

        if (tour != null) {
            return Optional.of(ItinerarySchedule.builder()
                    .itineraryDay(day)
                    .type(ItineraryScheduleType.TOUR.name())
                    .tour(tour)
                    .sequence(item.sequence())
                    .build());
        }

        if (StringUtils.hasText(item.title())) {
            if (StringUtils.hasText(item.contentId())) {
                log.info("추천 목록에 없는 contentId 는 일반 일정으로 저장합니다: contentId={}, title={}", item.contentId(), item.title());
            }
            return Optional.of(ItinerarySchedule.builder()
                    .itineraryDay(day)
                    .type(ItineraryScheduleType.NORMAL.name())
                    .title(item.title())
                    .description(item.description())
                    .sequence(item.sequence())
                    .build());
        }

        log.warn("contentId 와 title 이 모두 없어 상세 일정을 제외합니다: {}", item);
        return Optional.empty();
    }
}
