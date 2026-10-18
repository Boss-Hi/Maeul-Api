package com.bosshi.maeul.itinerary.request;

import com.bosshi.maeul.itinerary.type.ItineraryScheduleType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
public class ItineraryTourCreateRequest {
    /** TOUR | NORMAL, 기본값 TOUR */
    private ItineraryScheduleType type = ItineraryScheduleType.TOUR;

    /** TOUR 타입 필수 */
    private String contentId;

    /** NORMAL 타입 필수 */
    private String title;

    /** NORMAL 타입 필수 */
    private String description;

    private Integer sequence;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;
}
