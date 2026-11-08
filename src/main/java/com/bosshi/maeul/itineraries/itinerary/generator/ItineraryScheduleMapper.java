package com.bosshi.maeul.itineraries.itinerary.generator;

import com.bosshi.maeul.itineraries.itinerary.entity.ItineraryDay;
import com.bosshi.maeul.itineraries.itinerary.entity.ItinerarySchedule;
import com.bosshi.maeul.itineraries.itinerary.generator.impl.ItineraryGeneratorImpl.ItineraryTourJson;
import com.bosshi.maeul.itineraries.itinerary.type.ItineraryScheduleType;
import com.bosshi.maeul.itineraries.openapi.entity.Tour;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
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

    /**
     * @param windowStart 하루 시작 시간. 항목의 시간은 windowStart ~ windowEnd 안이어야 하며, 아니면 시간 없이 저장한다
     */
    static Optional<ItinerarySchedule> toSchedule(ItineraryDay day, ItineraryTourJson item, Function<String, Tour> tourResolver,
                                                  LocalTime windowStart, LocalTime windowEnd) {
        LocalTime startTime = parseTime(item.startTime());
        LocalTime endTime = parseTime(item.endTime());
        if (!isWithinWindow(startTime, endTime, windowStart, windowEnd)) {
            if (startTime != null || endTime != null) {
                log.warn("시간을 쓸 수 없어 시간 없이 저장합니다: start={}, end={}, window={}~{}",
                        item.startTime(), item.endTime(), windowStart, windowEnd);
            }
            startTime = null;
            endTime = null;
        }
        Tour tour = StringUtils.hasText(item.contentId()) ? tourResolver.apply(item.contentId()) : null;

        if (tour != null) {
            return Optional.of(ItinerarySchedule.builder()
                    .itineraryDay(day)
                    .type(ItineraryScheduleType.TOUR.name())
                    .tour(tour)
                    .sequence(item.sequence())
                    .startTime(startTime)
                    .endTime(endTime)
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
                    .startTime(startTime)
                    .endTime(endTime)
                    .build());
        }

        log.warn("contentId 와 title 이 모두 없어 상세 일정을 제외합니다: {}", item);
        return Optional.empty();
    }

    private static LocalTime parseTime(String text) {
        if (!StringUtils.hasText(text)) {
            return null;
        }
        try {
            return LocalTime.parse(text.trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private static boolean isWithinWindow(LocalTime start, LocalTime end, LocalTime windowStart, LocalTime windowEnd) {
        return start != null && end != null && start.isBefore(end)
                && (windowStart == null || !start.isBefore(windowStart))
                && (windowEnd == null || !end.isAfter(windowEnd));
    }
}
