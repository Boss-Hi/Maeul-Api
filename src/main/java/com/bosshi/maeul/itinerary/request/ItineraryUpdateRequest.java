package com.bosshi.maeul.itinerary.request;

import com.bosshi.maeul.itinerary.type.ItineraryStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ItineraryUpdateRequest {
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    private ItineraryStatus status;

    private String tourId;
}

