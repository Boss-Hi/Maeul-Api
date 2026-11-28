package com.bosshi.maeul.itineraries.itinerary.photomission;

import com.bosshi.maeul.itineraries.itinerary.event.ItineraryPeriodChangedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/** 일정을 만들거나 기간이 바뀌면 사진 미션을 붙이거나 기간을 맞춘다 */
@Component
@RequiredArgsConstructor
class PhotoMissionItineraryListener {

    private final PhotoMissionService photoMissionService;

    @EventListener
    void onItineraryPeriodChanged(ItineraryPeriodChangedEvent event) {
        photoMissionService.attach(event.itineraryId(), event.userId(), event.regionCode(), event.subdivisionCode(),
                event.startDate(), event.endDate());
    }
}
