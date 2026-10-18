package com.bosshi.maeul.itinerary.response;

import com.bosshi.maeul.itinerary.entity.Itinerary;
import com.bosshi.maeul.itinerary.entity.ItineraryDay;
import com.bosshi.maeul.itinerary.entity.ItinerarySchedule;
import com.bosshi.maeul.location.entity.LocationMapping;
import com.bosshi.maeul.openapi.response.TourResponse;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
        private String type;   // itinerary_schedule_types.code (e.g. "TOUR", "NORMAL")
        private Integer sequence;
        @JsonFormat(pattern = "HH:mm")
        private LocalTime startTime;
        @JsonFormat(pattern = "HH:mm")
        private LocalTime endTime;
        // TOUR 타입
        private TourResponse tour;
        // NORMAL 타입
        private String title;
        private String description;
        private LocationMappingResponse location;

        public static ItineraryScheduleResponse from(ItinerarySchedule schedule) {
            return from(schedule, Optional.empty());
        }

        public static ItineraryScheduleResponse from(ItinerarySchedule schedule, Optional<LocationMapping> locationOpt) {
            return ItineraryScheduleResponse.builder()
                    .id(schedule.getId())
                    .type(schedule.getType() != null ? schedule.getType() : null)
                    .sequence(schedule.getSequence())
                    .startTime(schedule.getStartTime())
                    .endTime(schedule.getEndTime())
                    .tour(schedule.getTour() != null ? TourResponse.from(schedule.getTour()) : null)
                    .title(schedule.getTitle())
                    .description(schedule.getDescription())
                    .location(locationOpt.map(LocationMappingResponse::from).orElse(null))
                    .build();
        }
    }

    @Getter
    @Builder
    public static class LocationMappingResponse {

        private String addressLine1;
        private String addressLine2;
        private String zipcode;
        private Double latitude;
        private Double longitude;
        private String region1Depth;
        private String region2Depth;

        public static LocationMappingResponse from(LocationMapping location) {
            return LocationMappingResponse.builder()
                    .addressLine1(location.getAddressLine1())
                    .addressLine2(location.getAddressLine2())
                    .zipcode(location.getZipcode())
                    .latitude(location.getLatitude())
                    .longitude(location.getLongitude())
                    .region1Depth(location.getRegion1Depth())
                    .region2Depth(location.getRegion2Depth())
                    .build();
        }
    }

}

