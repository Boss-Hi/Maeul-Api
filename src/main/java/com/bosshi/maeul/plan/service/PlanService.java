package com.bosshi.maeul.plan.service;

import com.bosshi.maeul.openapi.repository.FestivalRepository;
import com.bosshi.maeul.plan.dto.PlanGenerateDTO;
import com.bosshi.maeul.plan.entity.Itinerary;
import com.bosshi.maeul.plan.entity.Trip;
import com.bosshi.maeul.plan.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 여행 추천 시스템의 핵심 조율 서비스
 * <p>
 * 전체 여행 일정 추천 프로세스를 관리합니다:
 * 1. Trip 생성
 * 2. MainFestival 선택
 * 3. 관광지 필터링
 * 4. AI 일정 생성
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PlanService {

    private final TripRepository tripRepository;
    private final FestivalRepository festivalRepository;
    private final TourFilteringService venueFilteringService;
    private final ItineraryGenerationService itineraryGenerationService;

    /**
     * 사용자가 MainFestival을 선택하고 일정 생성을 요청합니다.
     *
     * @param dto MainFestival 선택 요청
     */
    public void generateItinerary(PlanGenerateDTO dto) {
        if (!dto.isValid()) {
            throw new IllegalArgumentException("입력 값이 유효하지 않습니다");
        }

        dto.setFestival(festivalRepository.findById(dto.getFestivalId())
                .orElseThrow(() -> new IllegalArgumentException("Festival을 찾을 수 없습니다: " + dto.getFestivalId())));

        // 관광지 필터링
        dto.setRecommendableFestivals(venueFilteringService.filterTourByCategories(dto));

        // AI 일정 생성
        itineraryGenerationService.generateItinerary(dto);
    }

    /**
     * Trip과 연결된 Itinerary를 조회합니다.
     *
     * @param tripId Trip ID
     * @return Itinerary 객체
     */
    @Transactional(readOnly = true)
    public Itinerary getItinerary(String tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("Trip을 찾을 수 없습니다: " + tripId));

        return trip.getItinerary();
    }
}

