package com.bosshi.maeul.plan.service;

import com.bosshi.maeul.common.utils.GeoUtils;
import com.bosshi.maeul.openapi.entity.Festival;
import com.bosshi.maeul.openapi.entity.TourCategory;
import com.bosshi.maeul.openapi.repository.FestivalRepository;
import com.bosshi.maeul.openapi.repository.TourCategoryRepository;
import com.bosshi.maeul.plan.dto.PlanGenerateDTO;
import com.bosshi.maeul.plan.entity.MainFestival;
import com.bosshi.maeul.plan.entity.Trip;
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
public class TourFilteringService {
    private final TourCategoryRepository categoryRepository;
    private final FestivalRepository festivalRepository;

    /**
     * Trip과 MainFestival 정보를 기반으로 필터링된 관광지 목록을 반환합니다.
     *
     * @param trip 여행 정보
     * @param mainFestival 주축제 정보
     * @return 필터링된 관광지 목록
     */
    public void filterVenuesByTripAndFestival(Trip trip, MainFestival mainFestival) {
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
    }

    /**
     * 카테고리별로 필터링된 관광지를 반환합니다.
     *
     * @param dto 일정 생성 요청 DTO
     * @return 필터링된 관광지 목록
     */
    public List<Festival> filterTourByCategories(PlanGenerateDTO dto) {
        List<String> selected = dto.getSelectedCategories();
        if (selected == null || selected.isEmpty()) {
            return List.of();
        }

        List<TourCategory> tourCategories = categoryRepository.findAll().stream()
                .filter(c -> selected.contains(c.getName()) || selected.contains(c.getCode()))
                .toList();

        if (tourCategories.isEmpty()) {
            log.warn("선택된 카테고리에 매핑되는 TourCategory를 찾을 수 없습니다.");
            return List.of();
        }

        // 기준이 되는 메인 페스티벌 위치 및 필터 정보 가져오기
        Festival mainFestival = dto.getFestival();
        Double baseLat = mainFestival != null ? mainFestival.getMapY() : null;
        Double baseLon = mainFestival != null ? mainFestival.getMapX() : null;

        Integer maxDistanceKm = 10; // 기본값 10km
        if (dto.getFilterSettings() != null && dto.getFilterSettings().getMaxDistanceKm() != null) {
            maxDistanceKm = dto.getFilterSettings().getMaxDistanceKm();
        }

        log.info("필터 반경: {} km, 기준 위치: ({}, {})", maxDistanceKm, baseLat, baseLon);

        // 전체 페스티벌/관광지 목록 가져오기
        List<Festival> allFestivals = festivalRepository.findAll();

        // 필터링 수행
        final Integer finalMaxDistanceKm = maxDistanceKm;
        return allFestivals.stream()
                // 메인 페스티벌 제외
                .filter(f -> mainFestival == null || !f.getContentId().equals(mainFestival.getContentId()))
                // 카테고리 매칭
                .filter(f -> matchesAnyCategory(f, tourCategories))
                // 거리 필터링 (위경도가 모두 있을 때만)
                .filter(f -> {
                    if (baseLat == null || baseLon == null || f.getMapY() == null || f.getMapX() == null) {
                        return true; // 좌표가 없으면 우선 포함
                    }
                    Double dist = GeoUtils.calculateDistance(baseLat, baseLon, f.getMapY(), f.getMapX());
                    return dist <= finalMaxDistanceKm;
                })
                .limit(10)
                .collect(Collectors.toList());
    }

    private boolean matchesAnyCategory(Festival f, List<TourCategory> categories) {
        for (TourCategory cat : categories) {
            String code = cat.getCode();
            Integer depth = cat.getDepth();
            if (depth == 1 && code != null && code.equalsIgnoreCase(f.getLclsSystm1())) {
                return true;
            }
            if (depth == 2 && code != null && code.equalsIgnoreCase(f.getLclsSystm2())) {
                return true;
            }
            if (depth == 3 && code != null && code.equalsIgnoreCase(f.getLclsSystm3())) {
                return true;
            }
            // Fallback to KTO contentTypeId matching
            if (cat.getContentTypeId() != null && cat.getContentTypeId().equals(f.getContentTypeId())) {
                return true;
            }
        }
        return false;
    }
}
