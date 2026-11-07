package com.bosshi.maeul.itinerary.generator.impl;

import com.bosshi.maeul.itinerary.dto.ItineraryGenerateDTO;
import com.bosshi.maeul.itinerary.entity.Itinerary;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.List;

/**
 * 일정 생성기 (AI 제공자 1개에 대응). 제공자 순서(ai.providers)에 따라 {@code ItineraryGeneratorRouter} 가 선택한다.
 */
public interface ItineraryGeneratorImpl {
    Itinerary generateItinerary(ItineraryGenerateDTO dto) throws JsonProcessingException;

    /**
     * ai.providers 에서 사용하는 제공자 이름 (claude, gemini, test)
     */
    String providerName();

    /**
     * 지금 호출 가능한지 (예: 활성 키 존재 여부)
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
            String description
    ) {
    }
}
