package com.bosshi.maeul.plan.service;

import com.bosshi.maeul.openapi.request.SearchFestivalRequest;
import com.bosshi.maeul.openapi.response.SearchFestivalResponse;
import com.bosshi.maeul.openapi.service.OpenApiService;
import com.bosshi.maeul.plan.domain.FilterSettings;
import com.bosshi.maeul.plan.domain.Itinerary;
import com.bosshi.maeul.plan.domain.Trip;
import com.bosshi.maeul.plan.repository.TripRepository;
import com.bosshi.maeul.plan.request.MainFestivalSelectRequest;
import com.bosshi.maeul.plan.request.TripCreateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
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
     * 사용자의 기본 정보로 Trip을 생성합니다.
     *
     * @param request 여행 생성 요청
     * @return 생성된 Trip의 ID
     */
    public String createTrip(TripCreateRequest request) {
        log.info("Trip 생성: 목적지={}, 기간={} ~ {}", request.getDestination(), request.getStartDate(), request.getEndDate());

        if (!request.isValid()) {
            throw new IllegalArgumentException("입력 값이 유효하지 않습니다");
        }

        // FilterSettings 생성
        FilterSettings filterSettings = FilterSettings.builder()
                .maxVenuesPerDay(
                        request.getFilterSettings() != null && request.getFilterSettings().getMaxVenuesPerDay() != null
                                ? request.getFilterSettings().getMaxVenuesPerDay()
                                : 5
                )
                .maxDistanceKm(
                        request.getFilterSettings() != null && request.getFilterSettings().getMaxDistanceKm() != null
                                ? request.getFilterSettings().getMaxDistanceKm()
                                : 10
                )
                .allowCategoryMixPerDay(
                        request.getFilterSettings() != null && request.getFilterSettings().getAllowCategoryMixPerDay() != null
                                ? request.getFilterSettings().getAllowCategoryMixPerDay()
                                : true
                )
                .isGlobal(true)
                .build();

        // Trip 생성
        Trip trip = Trip.builder()
                .gender(request.getGender())
                .birthDate(request.getBirthDate())
                .destination(request.getDestination())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .selectedCategories(String.join(",", request.getSelectedCategories()))
                .filterSettings(filterSettings)
                .build();

        Trip savedTrip = tripRepository.save(trip);
        log.info("Trip 생성 완료: ID={}", savedTrip.getId());

        return savedTrip.getId();
    }

    /**
     * 축제 목록을 조회합니다.
     *
     * @param destination 목적지
     * @return 축제 목록
     */
    public SearchFestivalResponse getFestivalsByDestination(SearchFestivalRequest destination) {
        log.info("축제 목록 조회: {}", destination);

        return openApiService.searchFestival(destination);
    }

    /**
     * 사용자가 MainFestival을 선택하고 일정 생성을 요청합니다.
     *
     * @param request MainFestival 선택 요청
     */
    @Async
    public void generateItineraryAsync(MainFestivalSelectRequest request) {
        log.info("일정 생성 시작: Trip ID={}, Festival={}", request.getTripId(), request.getTitle());

        try {
            generateItinerary(request);
        } catch (Exception e) {
            log.error("비동기 일정 생성 중 오류", e);
        }
    }

    /**
     * 사용자가 MainFestival을 선택하고 일정 생성을 요청합니다.
     *
     * @param request MainFestival 선택 요청
     */
    public void generateItinerary(MainFestivalSelectRequest request) {
        /*log.info("일정 생성 시작: Trip ID={}, Festival={}", request.getTripId(), request.getTitle());

        if (!request.isValid()) {
            throw new IllegalArgumentException("입력 값이 유효하지 않습니다");
        }

        // 1. Trip 조회
        Trip trip = tripRepository.findById(request.getTripId())
                .orElseThrow(() -> new IllegalArgumentException("Trip을 찾을 수 없습니다: " + request.getTripId()));

        // 2. MainFestival 생성 및 저장
        MainFestival mainFestival = MainFestival.builder()
                .contentId(request.getContentId())
                .title(request.getTitle())
                .category("축제/공연")
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .address1(request.getAddress1())
                .address2(request.getAddress2())
                .tel(request.getTel())
                .imageUrl(request.getImageUrl())
                .build();

        trip.setMainFestival(mainFestival);

        // 3. 관광지 필터링
        log.info("관광지 필터링 시작");
        List<Venue> filteredVenues = venueFilteringService.filterVenuesByTripAndFestival(trip, mainFestival);
        log.info("필터링된 관광지: {} 개", filteredVenues.size());

        // 4. AI 일정 생성
        log.info("AI 일정 생성 시작");
        itineraryGenerationService.generateItinerary(trip, mainFestival, filteredVenues);

        log.info("일정 생성 완료: Trip ID={}", trip.getId());*/
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

