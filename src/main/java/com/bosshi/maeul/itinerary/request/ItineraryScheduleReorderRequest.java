package com.bosshi.maeul.itinerary.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class ItineraryScheduleReorderRequest {

    private List<ScheduleOrderItem> schedules;

    @Getter
    @NoArgsConstructor
    public static class ScheduleOrderItem {
        private Long id;
        private Integer sequence;
    }
}

