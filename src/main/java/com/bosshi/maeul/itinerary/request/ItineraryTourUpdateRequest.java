package com.bosshi.maeul.itinerary.request;

import com.bosshi.maeul.itinerary.type.ItineraryScheduleType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
public class ItineraryTourUpdateRequest {
    /** 타입 변경 시 지정 */
    private ItineraryScheduleType type;

    /** TOUR 타입 */
    private String contentId;

    /** NORMAL 타입 */
    private String title;

    /** NORMAL 타입 */
    private String description;

    private Integer sequence;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;

    private Boolean clearTime;
}
