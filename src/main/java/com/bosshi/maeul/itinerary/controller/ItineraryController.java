package com.bosshi.maeul.itinerary.controller;

import com.bosshi.maeul.itinerary.dto.ItineraryGenerateDTO;
import com.bosshi.maeul.itinerary.request.ItineraryGenerateRequest;
import com.bosshi.maeul.itinerary.service.ItineraryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 여행 추천 REST API 컨트롤러
 * <p>
 * 여행 일정 추천 시스템의 API 엔드포인트를 제공합니다.
 */
@RestController
@RequestMapping("/api/itinerary")
@RequiredArgsConstructor
@Slf4j
public class ItineraryController {

    private final ItineraryService tripRecommendationService;

    /**
     * Step 3: MainFestival 선택 및 일정 생성
     * <p>
     * POST /api/v1/trips/{tripId}/generate-itinerary
     * 축제를 선택하고 AI 기반 일정을 생성합니다.
     *
     * @param request MainFestival 선택 요청
     * @return 생성 요청 결과
     */
    @PostMapping("/generate")
    public ResponseEntity<?> generate(@RequestBody ItineraryGenerateRequest request) {
        ItineraryGenerateDTO dto = ItineraryGenerateDTO.builder()
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .tourId(request.getTourId())
                .selectedCategories(List.of("VE06", "EV02", "AC01"))
                .filterSettings(new ItineraryGenerateDTO.FilterSettingsRequest(
                        5,
                        25,
                        true
                ))
                .build();

        // 일정 생성
        tripRecommendationService.generateItinerary(dto);

        return ResponseEntity.accepted().body("");
    }
}


