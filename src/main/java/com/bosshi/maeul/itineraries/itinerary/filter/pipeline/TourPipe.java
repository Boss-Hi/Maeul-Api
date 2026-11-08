package com.bosshi.maeul.itineraries.itinerary.filter.pipeline;

import com.bosshi.maeul.itineraries.itinerary.filter.TourFilterContext;

public interface TourPipe {
    TourFilterContext process(TourFilterContext context);
}