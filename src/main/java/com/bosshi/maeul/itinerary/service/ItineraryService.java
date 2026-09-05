package com.bosshi.maeul.itinerary.service;

import com.bosshi.maeul.itinerary.dto.ItineraryGenerateDTO;
import com.bosshi.maeul.itinerary.entity.Itinerary;
import com.bosshi.maeul.itinerary.filter.TourFilteringService;
import com.bosshi.maeul.itinerary.generator.ItineraryGenerator;
import com.bosshi.maeul.itinerary.repository.ItineraryRepository;
import com.bosshi.maeul.openapi.entity.Tour;
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
    private final TourRepository tourRepository;
    private final TourFilteringService tourFilteringService;
    private final ItineraryRepository itineraryRepository;
    private final ItineraryGenerator itineraryGenerator;

    /**
     * 사용자가 MainFestival을 선택하고 일정 생성을 요청합니다.
     *
     * @param dto MainFestival 선택 요청
     */
    public Itinerary generateItinerary(ItineraryGenerateDTO dto) {
        Tour tour = tourRepository.findByContentId(dto.getTourId())
                .orElseThrow(() -> new IllegalArgumentException("Tour를 찾을 수 없습니다: " + dto.getTourId()));
        dto.setTour(tour);
        dto.setRecommendableTours(tourFilteringService.filterTourByCategories(dto));

        Itinerary itinerary = itineraryGenerator.generateItinerary(dto);
        Itinerary savedItinerary = itineraryRepository.save(itinerary);
        log.info(
                "응답 파싱 및 DB 저장 완료: ID={}, 일수={}", savedItinerary.getId(), savedItinerary.getItineraryDays()
                        .size()
        );
        return savedItinerary;
    }

    public Itinerary findById(Long id) {
        return itineraryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Itinerary을 찾을 수 없습니다: " + id));
    }
}

