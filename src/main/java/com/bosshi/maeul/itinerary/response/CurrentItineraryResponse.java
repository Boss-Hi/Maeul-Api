package com.bosshi.maeul.itinerary.response;

import com.bosshi.maeul.itinerary.entity.Itinerary;
import com.bosshi.maeul.itinerary.entity.ItineraryMissionMapping;
import com.bosshi.maeul.itinerary.type.MissionStatus;
import com.bosshi.maeul.openapi.response.TourResponse;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class CurrentItineraryResponse {

    private Long id;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    private TourResponse tour;
    private List<ItineraryMissionMappingResponse> itineraryMissionMappings;

    public static CurrentItineraryResponse from(Itinerary itinerary) {
        if (itinerary == null) {
            return null;
        }

        return CurrentItineraryResponse.builder()
                .id(itinerary.getId())
                .startDate(itinerary.getStartDate())
                .endDate(itinerary.getEndDate())
                .tour(TourResponse.from(itinerary.getTour()))
                .itineraryMissionMappings(itinerary.getItineraryMissionMappings()
                        .stream()
                        .map(ItineraryMissionMappingResponse::from)
                        .toList())
                .build();
    }

    public static CurrentItineraryResponse empty() {
        return CurrentItineraryResponse.builder()
                .id(null)
                .startDate(null)
                .endDate(null)
                .tour(null)
                .itineraryMissionMappings(null)
                .build();
    }

    @Getter
    @Builder
    public static class ItineraryMissionMappingResponse {

        private MissionStatus status;
        private String name;
        private String description;

        public static ItineraryMissionMappingResponse from(ItineraryMissionMapping itineraryMissionMapping) {
            if (itineraryMissionMapping == null) {
                return null;
            }

            return ItineraryMissionMappingResponse.builder()
                    .status(itineraryMissionMapping.getStatus())
                    .name(itineraryMissionMapping.getMission().getName())
                    .description(itineraryMissionMapping.getMission().getDescription())
                    .build();
        }

    }


}

