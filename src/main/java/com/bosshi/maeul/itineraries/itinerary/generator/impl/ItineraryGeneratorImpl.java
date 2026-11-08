package com.bosshi.maeul.itineraries.itinerary.generator.impl;

import com.bosshi.maeul.itineraries.itinerary.dto.ItineraryGenerateDTO;
import com.bosshi.maeul.itineraries.itinerary.entity.Itinerary;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.List;

/**
 * 일정 생성기. {@code itinerary.generator} 설정(ai, test)으로 하나가 선택된다.
 */
public interface ItineraryGeneratorImpl {
    Itinerary generateItinerary(ItineraryGenerateDTO dto) throws JsonProcessingException;

    /**
     * 지금 호출 가능한지 (예: 사용 가능한 AI 제공자 존재 여부)
     */
    default boolean isAvailable() {
        return true;
    }

    record ItineraryDayJson(
            String date,
            Integer dayNumber,
            List<ItineraryTourJson> items
    ) {
    }

    /**
     * 상세 일정 항목.
     * contentId 가 추천 목록의 관광지를 가리키면 TOUR, 아니면 title/description 으로 NORMAL 일정이 된다.
     */
    record ItineraryTourJson(
            String contentId,
            Integer sequence,
            String title,
            String description,
            /** HH:mm. 하루 시간대 안의 방문 시간 */
            String startTime,
            String endTime
    ) {
    }
}
