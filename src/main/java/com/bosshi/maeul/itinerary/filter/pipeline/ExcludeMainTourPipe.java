package com.bosshi.maeul.itinerary.filter.pipeline;

import com.bosshi.maeul.itinerary.filter.TourFilterContext;
import com.bosshi.maeul.openapi.entity.Tour;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(1)
public class ExcludeMainTourPipe implements TourPipe {

    @Override
    public TourFilterContext process(TourFilterContext context) {
        Tour mainTour = context.getTour();
        if (mainTour == null) return context;

        List<Tour> filtered = context.getCandidateTours().stream()
                .filter(f -> !f.getContentId().equals(mainTour.getContentId()))
                .toList();

        context.setCandidateTours(filtered);
        return context;
    }
}