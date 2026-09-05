package com.bosshi.maeul.itinerary.service;

import com.bosshi.maeul.itinerary.dto.ItineraryGenerateDTO;
import com.bosshi.maeul.itinerary.entity.Itinerary;
import com.bosshi.maeul.itinerary.repository.ItineraryRepository;
import com.bosshi.maeul.openapi.repository.TourRepository;
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
    public Itinerary generateItinerary(ItineraryGenerateDTO dto) {
        return null;
    }

    public Itinerary findById(Long id) {
        return itineraryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Itinerary을 찾을 수 없습니다: " + id));
    }
}

