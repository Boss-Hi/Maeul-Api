package com.bosshi.maeul.itinerary.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ItineraryDayCreateRequest {
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;
    private Integer dayNumber;
}

