package com.bosshi.maeul.plan.service;

import com.bosshi.maeul.openapi.repository.TourRepository;
import com.bosshi.maeul.plan.dto.ItineraryGenerateDTO;
import com.bosshi.maeul.plan.repository.ItineraryRepository;
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
public class ItineraryService {

    private final ItineraryRepository itineraryRepository;
    private final TourRepository festivalRepository;
    private final TourFilteringService tourFilteringService;
    private final ItineraryGenerationService itineraryGenerationService;

    /**
     * 사용자가 MainFestival을 선택하고 일정 생성을 요청합니다.
     *
     * @param dto MainFestival 선택 요청
     */
    public void generateItinerary(ItineraryGenerateDTO dto) {
        if (!dto.isValid()) {
            throw new IllegalArgumentException("입력 값이 유효하지 않습니다");
        }

        dto.setTour(festivalRepository.findById(dto.getTourId())
                .orElseThrow(() -> new IllegalArgumentException("Tour을 찾을 수 없습니다: " + dto.getTourId())));

        // 관광지 필터링
        dto.setRecommendableTours(tourFilteringService.filterTourByCategories(dto));

        // AI 일정 생성
        itineraryGenerationService.generateItinerary(dto);
    }

}

