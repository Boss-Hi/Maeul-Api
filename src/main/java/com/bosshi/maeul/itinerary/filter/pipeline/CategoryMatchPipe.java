package com.bosshi.maeul.itinerary.filter.pipeline;

import com.bosshi.maeul.itinerary.filter.TourFilterContext;
import com.bosshi.maeul.openapi.entity.Tour;
import com.bosshi.maeul.openapi.entity.TourCategory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(2)
public class CategoryMatchPipe implements TourPipe {

    @Override
    public TourFilterContext process(TourFilterContext context) {
        List<TourCategory> categories = context.getSelectedCategories();
        if (categories == null || categories.isEmpty()) {
            return context;
        }

        List<Tour> filtered = context.getCandidateTours().stream()
                .filter(tour -> matchesAnyCategory(tour, categories))
                .toList();

        context.setCandidateTours(filtered);
        return context;
    }

    private boolean matchesAnyCategory(Tour f, List<TourCategory> categories) {
        for (TourCategory cat : categories) {
            String code = cat.getCode();
            Integer depth = cat.getDepth();
            if (depth == 1 && code != null && code.equalsIgnoreCase(f.getLclsSystm1())) return true;
            if (depth == 2 && code != null && code.equalsIgnoreCase(f.getLclsSystm2())) return true;
            if (depth == 3 && code != null && code.equalsIgnoreCase(f.getLclsSystm3())) return true;
            if (cat.getContentTypeId() != null && cat.getContentTypeId().equals(f.getContentTypeId())) return true;
        }
        return false;
    }
}