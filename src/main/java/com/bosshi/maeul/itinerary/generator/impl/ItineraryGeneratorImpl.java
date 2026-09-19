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

    record ItineraryTourJson(
            String contentId,
            Integer sequence
    ) {
    }
}
