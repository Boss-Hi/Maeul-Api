package com.bosshi.maeul.territory.service;

import com.bosshi.maeul.itineraries.itinerary.event.ItineraryPeriodChangedEvent;
import com.bosshi.maeul.territory.entity.FatItineraryPeriod;
import com.bosshi.maeul.territory.repository.FatItineraryPeriodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 일정이 만들어지거나 바뀌면 그 일정 기간의 복사본을 갱신한다. 시군구가 없는 일정은 일정 기간을 남기지 않는다.
 */
@Component
@RequiredArgsConstructor
public class FatItineraryPeriodRecorder {

    private final FatItineraryPeriodRepository periodRepository;

    @EventListener
    @Transactional
    public void onPeriodChanged(ItineraryPeriodChangedEvent event) {
        FatItineraryPeriod existing = periodRepository.findByItineraryId(event.itineraryId()).orElse(null);
        if (event.regionCode() == null || event.subdivisionCode() == null) {
            if (existing != null) {
                periodRepository.delete(existing);
            }
            return;
        }
        FatItineraryPeriod period = existing != null ? existing : FatItineraryPeriod.builder().itineraryId(event.itineraryId()).build();
        period.setUserId(event.userId());
        period.setRegionCode(event.regionCode());
        period.setSubdivisionCode(event.subdivisionCode());
        period.setStartDate(event.startDate());
        period.setEndDate(event.endDate());
        periodRepository.save(period);
    }
}
