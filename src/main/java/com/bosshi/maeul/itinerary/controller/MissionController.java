package com.bosshi.maeul.itinerary.controller;

import com.bosshi.maeul.auth.CustomUserDetails;
import com.bosshi.maeul.common.response.ApiResponse;
import com.bosshi.maeul.itinerary.entity.Itinerary;
import com.bosshi.maeul.itinerary.response.ItineraryResponse;
import com.bosshi.maeul.itinerary.service.ItineraryMissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 일정과 관련된 미션 정보를 제공
 */
@RestController
@RequestMapping("/api/missions")
@RequiredArgsConstructor
@Slf4j
public class MissionController {
    private final ItineraryMissionService itineraryMissionService;

    @GetMapping("/current")
    public ResponseEntity<ApiResponse<ItineraryResponse>> current(@AuthenticationPrincipal CustomUserDetails userDetails) {
        // Itinerary itinerary = itineraryMissionService.findById(itineraryId);
        Itinerary itinerary = null;
        return ApiResponse.success(ItineraryResponse.from(itinerary));
    }

}


