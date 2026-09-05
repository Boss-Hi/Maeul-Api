package com.bosshi.maeul.itinerary.filter.pipeline;

import com.bosshi.maeul.itinerary.filter.TourFilterContext;
import com.bosshi.maeul.location.service.GeoService;
import com.bosshi.maeul.openapi.entity.Tour;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(3)
public class DistanceFilterPipe implements TourPipe {

    @Override
    public TourFilterContext process(TourFilterContext context) {
        Tour mainTour = context.getTour();
        if (mainTour == null || mainTour.getMapY() == null || mainTour.getMapX() == null) {
            return context;
        }

        Double baseLat = mainTour.getMapY();
        Double baseLon = mainTour.getMapX();

        Integer maxDistanceKm = 15;
        if (context.getFilterSettings() != null && context.getFilterSettings().getMaxDistanceKm() != null) {
            maxDistanceKm = context.getFilterSettings().getMaxDistanceKm();
        }

        final Integer limitDistance = maxDistanceKm;
        List<Tour> filtered = context.getCandidateTours().stream()
                .filter(f -> {
                    if (f.getMapY() == null || f.getMapX() == null) return true;
                    Double dist = GeoService.calculateDistance(baseLat, baseLon, f.getMapY(), f.getMapX());
                    return dist <= limitDistance;
                })
                .toList();

        context.setCandidateTours(filtered);
        return context;
    }
}