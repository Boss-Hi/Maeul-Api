package com.bosshi.maeul.plan.service;

import com.bosshi.maeul.common.utils.GeoUtils;
import com.bosshi.maeul.openapi.entity.Tour;
import com.bosshi.maeul.openapi.entity.TourCategory;
import com.bosshi.maeul.openapi.repository.TourCategoryRepository;
import com.bosshi.maeul.openapi.repository.TourRepository;
import com.bosshi.maeul.plan.dto.ItineraryGenerateDTO;
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
    private final TourRepository festivalRepository;

    /**
     * 카테고리별로 필터링된 관광지를 반환합니다.
     *
     * @param dto 일정 생성 요청 DTO
     * @return 필터링된 관광지 목록
     */
    public List<Tour> filterTourByCategories(ItineraryGenerateDTO dto) {
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

        // 기준이 되는 메인 투어 위치 및 필터 정보 가져오기
        Tour mainTour = dto.getTour();
        Double baseLat = mainTour != null ? mainTour.getMapY() : null;
        Double baseLon = mainTour != null ? mainTour.getMapX() : null;

        Integer maxDistanceKm = 15; // 기본값 15km
        if (dto.getFilterSettings() != null && dto.getFilterSettings().getMaxDistanceKm() != null) {
            maxDistanceKm = dto.getFilterSettings().getMaxDistanceKm();
        }

        log.info("필터 반경: {} km, 기준 위치: ({}, {})", maxDistanceKm, baseLat, baseLon);

        // 전체 투어 목록 가져오기
        List<Tour> allTours = festivalRepository.findAll();

        // 필터링 수행
        final Integer finalMaxDistanceKm = maxDistanceKm;
        return allTours.stream()
                // 메인 투어 제외
                .filter(f -> mainTour == null || !f.getContentId().equals(mainTour.getContentId()))
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

    private boolean matchesAnyCategory(Tour f, List<TourCategory> categories) {
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
