package com.bosshi.maeul.itinerary.controller;

import com.bosshi.maeul.auth.CustomUserDetails;
import com.bosshi.maeul.common.response.ApiResponse;
import com.bosshi.maeul.itinerary.entity.Itinerary;
import com.bosshi.maeul.itinerary.entity.ItinerarySchedule;
import com.bosshi.maeul.itinerary.request.ItineraryTourCreateRequest;
import com.bosshi.maeul.itinerary.request.ItineraryTourUpdateRequest;
import com.bosshi.maeul.itinerary.response.ItineraryResponse;
import com.bosshi.maeul.itinerary.service.ItineraryScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 여행 추천 REST API 컨트롤러
 * <p>
 * 여행 일정 추천 시스템의 API 엔드포인트를 제공합니다.
 */
@RestController
@RequestMapping("/api/itinerary/{itinerary}/days/{itineraryDay}/schedules")
@RequiredArgsConstructor
@Slf4j
public class ItineraryScheduleController {
    private final ItineraryScheduleService itineraryScheduleService;

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ItineraryResponse.ItineraryScheduleResponse>> show(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long itinerary,
            @PathVariable Long itineraryDay,
            @PathVariable Long id
    ) {
        ItinerarySchedule itinerarySchedule = itineraryScheduleService.show(itinerary, itineraryDay, id, userDetails.getId());
        return ApiResponse.success(ItineraryResponse.ItineraryScheduleResponse.from(itinerarySchedule));
    }

    @PostMapping("/")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ItineraryResponse>> addTourItem(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @PathVariable Long dayId,
            @RequestBody ItineraryTourCreateRequest request
    ) {
        Itinerary itinerary = itineraryScheduleService.addTourItem(id, dayId, userDetails.getId(), request);
        return ApiResponse.success(ItineraryResponse.from(itinerary));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ItineraryResponse>> updateTourItem(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @PathVariable Long dayId,
            @PathVariable Long tourId,
            @RequestBody ItineraryTourUpdateRequest request
    ) {
        Itinerary itinerary = itineraryScheduleService.updateTourItem(id, dayId, tourId, userDetails.getId(), request);
        return ApiResponse.success(ItineraryResponse.from(itinerary));
    }
}


