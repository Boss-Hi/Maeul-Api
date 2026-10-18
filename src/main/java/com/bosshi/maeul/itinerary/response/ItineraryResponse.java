package com.bosshi.maeul.itinerary.response;

import com.bosshi.maeul.itinerary.entity.Itinerary;
import com.bosshi.maeul.itinerary.entity.ItineraryDay;
import com.bosshi.maeul.itinerary.entity.ItinerarySchedule;
import com.bosshi.maeul.openapi.response.TourResponse;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;
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
        private Integer scheduleCount;
        private List<ItineraryScheduleResponse> schedules;

        public static ItineraryDayResponse from(ItineraryDay day) {
            List<ItineraryScheduleResponse> scheduleResponses = day.getItinerarySchedules() != null
                    ? day.getItinerarySchedules().stream()
                    .map(ItineraryScheduleResponse::from)
                    .toList()
                    : Collections.emptyList();

            return ItineraryDayResponse.builder()
                    .id(day.getId())
                    .date(day.getDate())
                    .dayNumber(day.getDayNumber())
                    .scheduleCount(day.getItineraryScheduleCount())
                    .schedules(scheduleResponses)
                    .build();
        }
    }

    @Getter
    @Builder
    public static class ItineraryScheduleResponse {

        private Long id;
        private Integer sequence;
        @JsonFormat(pattern = "HH:mm")
        private LocalTime startTime;
        @JsonFormat(pattern = "HH:mm")
        private LocalTime endTime;
        private TourResponse tour;

        public static ItineraryScheduleResponse from(ItinerarySchedule schedule) {
            return ItineraryScheduleResponse.builder()
                    .id(schedule.getId())
                    .sequence(schedule.getSequence())
                    .startTime(schedule.getStartTime())
                    .endTime(schedule.getEndTime())
                    .tour(TourResponse.from(schedule.getTour()))
                    .build();
        }
    }

}

