package com.bosshi.maeul.itinerary.filter.pipeline;

import com.bosshi.maeul.itinerary.filter.TourFilterContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TourPipeline {

    private final List<TourPipe> pipes; // Spring이 @Order 순서대로 자동으로 주입함

    public TourFilterContext execute(TourFilterContext context) {
        for (TourPipe pipe : pipes) {
            context = pipe.process(context);
        }
        return context;
    }
}