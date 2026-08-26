package com.bosshi.maeul.plan.service;

import com.bosshi.maeul.category.domain.TourCategory;
import com.bosshi.maeul.category.repository.TourCategoryRepository;
import com.bosshi.maeul.common.utils.GeoUtils;
import com.bosshi.maeul.openapi.domain.Venue;
import com.bosshi.maeul.openapi.repository.VenueRepository;
import com.bosshi.maeul.plan.domain.MainFestival;
import com.bosshi.maeul.plan.domain.Trip;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 관광지 필터링 서비스
 *
 * 한국관광공사 API에서 조회한 데이터를 필터링하여
 * 사용자의 조건에 맞는 관광지 목록을 반환합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VenueFilteringService {

    private final VenueRepository venueRepository;
    private final TourCategoryRepository categoryRepository;
    private final KoreaOpenApiService koreaOpenApiService;

    /**
     * Trip과 MainFestival 정보를 기반으로 필터링된 관광지 목록을 반환합니다.
     *
     * @param trip 여행 정보
     * @param mainFestival 주축제 정보
     * @return 필터링된 관광지 목록
     */
    public List<Venue> filterVenuesByTripAndFestival(Trip trip, MainFestival mainFestival) {
        log.info("필터링 시작: Trip ID={}, Festival ID={}", trip.getId(), mainFestival.getId());

        // 1. 사용자가 선택한 카테고리 목록 가져오기
        List<String> selectedCategories = trip.getSelectedCategoriesList();
        log.info("선택된 카테고리: {}", selectedCategories);

        // 2. 선택된 카테고리에 해당하는 contentTypeId 목록 가져오기
        List<TourCategory> categories = selectedCategories.stream()
                .flatMap(categoryName -> categoryRepository.findAll().stream()
                        .filter(c -> c.getName().equals(categoryName)))
                .collect(Collectors.toList());

        log.info("매핑된 카테고리 수: {}", categories.size());

        // 3. API에서 실시간으로 데이터 조회 (향후 캐싱 추가)
        List<Venue> allVenues = koreaOpenApiService.searchVenuesByAreaAndCategories(
                trip.getDestination(),
                categories
        );
        log.info("API 조회 결과: {} 개 관광지", allVenues.size());

        // 4. MainFestival 주변 반경 내의 관광지만 필터링
        Integer maxDistanceKm = trip.getFilterSettings().getMaxDistanceKm();
        List<Venue> filteredByDistance = allVenues.stream()
                .filter(venue ->
                        GeoUtils.isWithinRadius(
                                mainFestival.getLatitude(),
                                mainFestival.getLongitude(),
                                venue.getMapy(),
                                venue.getMapx(),
                                maxDistanceKm
                        )
                )
                .collect(Collectors.toList());

        log.info("반경 {} km 내 필터링 결과: {} 개", maxDistanceKm, filteredByDistance.size());

        // 5. 중복 제거 (contentId 기준)
        List<Venue> uniqueVenues = filteredByDistance.stream()
                .collect(Collectors.toMap(
                        Venue::getContentId,
                        v -> v,
                        (v1, v2) -> v1
                ))
                .values()
                .stream()
                .collect(Collectors.toList());

        log.info("중복 제거 후: {} 개", uniqueVenues.size());

        return uniqueVenues;
    }

    /**
     * 카테고리별로 필터링된 관광지를 반환합니다.
     *
     * @param venues 전체 관광지 목록
     * @param categoryName 카테고리명
     * @return 해당 카테고리의 관광지 목록
     */
    public List<Venue> filterVenuesByCategory(List<Venue> venues, String categoryName) {
        Category category = categoryRepository.findByName(categoryName)
                .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다: " + categoryName));

        return venues.stream()
                .filter(v -> v.getCategory().equals(categoryName))
                .collect(Collectors.toList());
    }

    /**
     * 관광지 목록을 거리순으로 정렬합니다.
     *
     * @param venues 관광지 목록
     * @param baseLat 기준 위도
     * @param baseLon 기준 경도
     * @return 거리순으로 정렬된 관광지 목록
     */
    public List<Venue> sortVenuesByDistance(List<Venue> venues, Double baseLat, Double baseLon) {
        return venues.stream()
                .sorted((v1, v2) -> {
                    Double distance1 = GeoUtils.calculateDistance(baseLat, baseLon, v1.getMapy(), v1.getMapx());
                    Double distance2 = GeoUtils.calculateDistance(baseLat, baseLon, v2.getMapy(), v2.getMapx());
                    return distance1.compareTo(distance2);
                })
                .collect(Collectors.toList());
    }
}

