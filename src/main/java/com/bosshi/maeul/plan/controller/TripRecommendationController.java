package com.bosshi.maeul.plan.controller;

import com.bosshi.maeul.openapi.domain.Venue;
import com.bosshi.maeul.plan.domain.Itinerary;
import com.bosshi.maeul.plan.domain.ItineraryDay;
import com.bosshi.maeul.plan.domain.ItineraryVenue;
import com.bosshi.maeul.plan.request.MainFestivalSelectRequest;
import com.bosshi.maeul.plan.request.TripCreateRequest;
import com.bosshi.maeul.plan.response.FestivalListResponse;
import com.bosshi.maeul.plan.response.ItineraryResponse;
import com.bosshi.maeul.plan.service.TripRecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 여행 추천 REST API 컨트롤러
 *
 * 여행 일정 추천 시스템의 API 엔드포인트를 제공합니다.
 */
@RestController
@RequestMapping("/api/v1/trips")
@RequiredArgsConstructor
@Slf4j
public class TripRecommendationController {

    private final TripRecommendationService tripRecommendationService;

    /**
     * Step 1: Trip 생성
     *
     * POST /api/v1/trips
     * 사용자의 기본 정보로 여행을 생성합니다.
     *
     * @param request 여행 생성 요청
     * @return Trip ID
     */
    @PostMapping
    public ResponseEntity<?> createTrip(@RequestBody TripCreateRequest request) {
        log.info("Trip 생성 요청: {}", request.getDestination());

        try {
            String tripId = tripRecommendationService.createTrip(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    new CreateTripResponse(tripId, request.getDestination())
            );
        } catch (IllegalArgumentException e) {
            log.error("입력 값 검증 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    new ErrorResponse("INVALID_INPUT", e.getMessage())
            );
        } catch (Exception e) {
            log.error("Trip 생성 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ErrorResponse("INTERNAL_ERROR", e.getMessage())
            );
        }
    }

    /**
     * Step 2: 축제 목록 조회
     *
     * GET /api/v1/trips/{tripId}/festivals
     * 지정된 지역의 축제 목록을 조회합니다.
     *
     * @param destination 목적지
     * @return 축제 목록
     */
    @GetMapping("/festivals")
    public ResponseEntity<?> getFestivalsByDestination(
            @RequestParam String destination) {
        log.info("축제 목록 조회: {}", destination);

        try {
            List<Venue> festivals = tripRecommendationService.getFestivalsByDestination(destination);

            List<FestivalListResponse.FestivalInfo> festivalInfos = festivals.stream()
                    .map(venue -> FestivalListResponse.FestivalInfo.builder()
                            .contentId(venue.getContentId())
                            .title(venue.getTitle())
                            .startDate(null) // TODO: 축제 날짜 추가
                            .endDate(null)
                            .latitude(venue.getMapy())
                            .longitude(venue.getMapx())
                            .address1(venue.getAddress1())
                            .address2(venue.getAddress2())
                            .imageUrl(venue.getFirstImage())
                            .tel(venue.getTel())
                            .build()
                    )
                    .collect(Collectors.toList());

            FestivalListResponse response = FestivalListResponse.builder()
                    .festivals(festivalInfos)
                    .totalCount(festivalInfos.size())
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("축제 조회 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ErrorResponse("FETCH_FAILED", e.getMessage())
            );
        }
    }

    /**
     * Step 3: MainFestival 선택 및 일정 생성
     *
     * POST /api/v1/trips/{tripId}/generate-itinerary
     * 축제를 선택하고 AI 기반 일정을 생성합니다.
     *
     * @param request MainFestival 선택 요청
     * @return 생성 요청 결과
     */
    @PostMapping("/{tripId}/generate-itinerary")
    public ResponseEntity<?> generateItinerary(
            @PathVariable String tripId,
            @RequestBody MainFestivalSelectRequest request) {
        log.info("일정 생성 요청: Trip ID={}", tripId);

        try {
            request.setTripId(tripId);

            // 비동기로 일정 생성 (백그라운드 처리)
            tripRecommendationService.generateItineraryAsync(request);

            return ResponseEntity.accepted().body(
                    new GenerateItineraryResponse(tripId, "일정 생성이 진행 중입니다. 잠시 후 다시 조회해주세요.")
            );
        } catch (IllegalArgumentException e) {
            log.error("입력 값 검증 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    new ErrorResponse("INVALID_INPUT", e.getMessage())
            );
        } catch (Exception e) {
            log.error("일정 생성 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ErrorResponse("GENERATION_FAILED", e.getMessage())
            );
        }
    }

    /**
     * Step 4: 생성된 일정 조회
     *
     * GET /api/v1/trips/{tripId}/itinerary
     * 생성된 여행 일정을 조회합니다.
     *
     * @param tripId Trip ID
     * @return 생성된 일정
     */
    @GetMapping("/{tripId}/itinerary")
    public ResponseEntity<?> getItinerary(@PathVariable String tripId) {
        log.info("일정 조회: Trip ID={}", tripId);

        try {
            Itinerary itinerary = tripRecommendationService.getItinerary(tripId);

            if (itinerary == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        new ErrorResponse("NOT_FOUND", "일정을 찾을 수 없습니다")
                );
            }

            ItineraryResponse response = convertToResponse(itinerary);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Trip 찾기 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ErrorResponse("NOT_FOUND", e.getMessage())
            );
        } catch (Exception e) {
            log.error("일정 조회 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ErrorResponse("FETCH_FAILED", e.getMessage())
            );
        }
    }

    /**
     * Itinerary를 ItineraryResponse로 변환합니다.
     */
    private ItineraryResponse convertToResponse(Itinerary itinerary) {
        List<ItineraryResponse.ItineraryDayResponse> dayResponses = itinerary.getDays().stream()
                .map(day -> convertDayToResponse(day))
                .collect(Collectors.toList());

        return ItineraryResponse.builder()
                .tripId(itinerary.getTrip().getId())
                .destination(itinerary.getTrip().getDestination())
                .startDate(itinerary.getTrip().getStartDate())
                .endDate(itinerary.getTrip().getEndDate())
                .mainFestival(
                        itinerary.getTrip().getMainFestival() != null ?
                                ItineraryResponse.MainFestivalInfo.builder()
                                        .name(itinerary.getTrip().getMainFestival().getTitle())
                                        .category(itinerary.getTrip().getMainFestival().getCategory())
                                        .startDate(itinerary.getTrip().getMainFestival().getStartDate())
                                        .endDate(itinerary.getTrip().getMainFestival().getEndDate())
                                        .build()
                                : null
                )
                .summary(itinerary.getSummary())
                .days(dayResponses)
                .build();
    }

    /**
     * ItineraryDay를 ItineraryDayResponse로 변환합니다.
     */
    private ItineraryResponse.ItineraryDayResponse convertDayToResponse(ItineraryDay day) {
        List<ItineraryResponse.ItineraryVenueResponse> venueResponses = day.getVenues().stream()
                .map(venue -> ItineraryResponse.ItineraryVenueResponse.builder()
                        .name(venue.getName())
                        .category(venue.getCategory())
                        .visitTime(venue.getVisitTime())
                        .duration(venue.getDuration())
                        .description(venue.getDescription())
                        .sequence(venue.getSequence())
                        .build()
                )
                .collect(Collectors.toList());

        return ItineraryResponse.ItineraryDayResponse.builder()
                .date(day.getDate())
                .dayNumber(day.getDayNumber())
                .theme(day.getTheme())
                .venues(venueResponses)
                .build();
    }

    // ==================== Inner Response Classes ====================

    @lombok.Getter
    @lombok.Setter
    @lombok.AllArgsConstructor
    public static class CreateTripResponse {
        private String tripId;
        private String destination;
    }

    @lombok.Getter
    @lombok.Setter
    @lombok.AllArgsConstructor
    public static class GenerateItineraryResponse {
        private String tripId;
        private String message;
    }

    @lombok.Getter
    @lombok.Setter
    @lombok.AllArgsConstructor
    public static class ErrorResponse {
        private String code;
        private String message;
    }
}

