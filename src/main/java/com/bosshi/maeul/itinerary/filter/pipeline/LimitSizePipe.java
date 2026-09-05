package com.bosshi.maeul.itinerary.filter.pipeline;

import com.bosshi.maeul.itinerary.filter.TourFilterContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(4)
public class LimitSizePipe implements TourPipe {

    private static final int DEFAULT_LIMIT = 10;

    @Override
    public TourFilterContext process(TourFilterContext context) {
        var limited = context.getCandidateTours().stream()
                .limit(DEFAULT_LIMIT)
                .toList();

        context.setCandidateTours(limited);
        return context;
    }
}