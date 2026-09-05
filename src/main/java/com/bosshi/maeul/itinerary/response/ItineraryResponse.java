package com.bosshi.maeul.itinerary.response;

import com.bosshi.maeul.itinerary.entity.Itinerary;
import com.bosshi.maeul.itinerary.entity.ItineraryDay;
import com.bosshi.maeul.itinerary.entity.ItineraryTour;
import com.bosshi.maeul.openapi.response.TourResponse;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Getter
@Builder
public class ItineraryResponse {

    private Long id;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    private TourResponse tour;

    private List<ItineraryDayResponse> days;

    public static ItineraryResponse from(Itinerary itinerary) {
        if (itinerary == null) {
            return null;
        }

        List<ItineraryDayResponse> dayResponses = itinerary.getItineraryDays() != null
                ? itinerary.getItineraryDays().stream()
                .map(ItineraryDayResponse::from)
                .toList()
                : Collections.emptyList();

        return ItineraryResponse.builder()
                .id(itinerary.getId())
                .startDate(itinerary.getStartDate())
                .endDate(itinerary.getEndDate())
                .tour(TourResponse.from(itinerary.getTour()))
                .days(dayResponses)
                .build();
    }

    @Getter
    @Builder
    public static class ItineraryDayResponse {

        private Long id;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate date;

        private Integer dayNumber;
        private Integer tourCount;
        private List<ItineraryTourResponse> tours;

        public static ItineraryDayResponse from(ItineraryDay day) {
            List<ItineraryTourResponse> tourResponses = day.getItineraryTours() != null
                    ? day.getItineraryTours().stream()
                    .map(ItineraryTourResponse::from)
                    .toList()
                    : Collections.emptyList();

            return ItineraryDayResponse.builder()
                    .id(day.getId())
                    .date(day.getDate())
                    .dayNumber(day.getDayNumber())
                    .tourCount(day.getItineraryTourCount())
                    .tours(tourResponses)
                    .build();
        }
    }

    @Getter
    @Builder
    public static class ItineraryTourResponse {

        private Long id;
        private String contentId;
        private Integer sequence;
        private TourResponse tour;

        public static ItineraryTourResponse from(ItineraryTour tour) {
            return ItineraryTourResponse.builder()
                    .id(tour.getId())
                    .sequence(tour.getSequence())
                    .tour(TourResponse.from(tour.getTour()))
                    .build();
        }
    }

}

