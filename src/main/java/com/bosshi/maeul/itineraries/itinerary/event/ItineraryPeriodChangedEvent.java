package com.bosshi.maeul.itineraries.itinerary.event;

import org.springframework.modulith.NamedInterface;

import java.time.LocalDate;

/**
 * 일정이 만들어지거나 날짜·주축제가 바뀌었을 때 보낸다.
 * 시군구는 주축제의 시도 코드(regionCode)와 시군구 코드(subdivisionCode)이며, 주축제가 없거나 코드가 비어 있으면 둘 다 null 이다.
 */
@NamedInterface
public record ItineraryPeriodChangedEvent(
        Long itineraryId,
        Long userId,
        String regionCode,
        String subdivisionCode,
        LocalDate startDate,
        LocalDate endDate
) {
}
