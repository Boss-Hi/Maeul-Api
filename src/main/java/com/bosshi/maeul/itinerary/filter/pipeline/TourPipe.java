package com.bosshi.maeul.itinerary.filter.pipeline;

import com.bosshi.maeul.itinerary.filter.TourFilterContext;

public interface TourPipe {
    TourFilterContext process(TourFilterContext context);
}