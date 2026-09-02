package com.bosshi.maeul.plan.service;

import com.bosshi.maeul.openapi.service.OpenApiService;
import com.bosshi.maeul.plan.dto.TripGenerateDTO;
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
public class TripRecommendationService {

    private final TripRepository tripRepository;
    private final OpenApiService openApiService;
    private final VenueFilteringService venueFilteringService;
    private final ItineraryGenerationService itineraryGenerationService;

    /**
     * 사용자가 MainFestival을 선택하고 일정 생성을 요청합니다.
     *
     * @param dto MainFestival 선택 요청
     */
    public void generateItinerary(TripGenerateDTO dto) {
        if (!dto.isValid()) {
            throw new IllegalArgumentException("입력 값이 유효하지 않습니다");
        }

        // 관광지 필터링
        /*log.info("관광지 필터링 시작");
        List<Venue> filteredVenues = venueFilteringService.filterVenuesByTripAndFestival(trip, mainFestival);
        log.info("필터링된 관광지: {} 개", filteredVenues.size());*/

        // 4. AI 일정 생성
        // log.info("AI 일정 생성 시작");
        // itineraryGenerationService.generateItinerary(trip, mainFestival, filteredVenues);
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

