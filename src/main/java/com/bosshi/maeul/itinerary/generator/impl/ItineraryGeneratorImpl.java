package com.bosshi.maeul.itinerary.generator.impl;

import com.bosshi.maeul.itinerary.dto.ItineraryGenerateDTO;
import com.bosshi.maeul.itinerary.entity.Itinerary;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.List;

public interface ItineraryGeneratorImpl {
    Itinerary generateItinerary(ItineraryGenerateDTO dto) throws JsonProcessingException;

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
            String description
    ) {
    }
}
