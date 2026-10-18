package com.bosshi.maeul.itinerary.controller;

import com.bosshi.maeul.auth.CustomUserDetails;
import com.bosshi.maeul.common.response.ApiResponse;
import com.bosshi.maeul.itinerary.entity.ItinerarySchedule;
import com.bosshi.maeul.itinerary.request.ItineraryTourCreateRequest;
import com.bosshi.maeul.itinerary.request.ItineraryTourUpdateRequest;
import com.bosshi.maeul.itinerary.response.ItineraryResponse;
import com.bosshi.maeul.itinerary.service.ItineraryScheduleService;
import com.bosshi.maeul.location.entity.LocationMapping;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

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
        Optional<LocationMapping> location = itineraryScheduleService.getLocation(itinerarySchedule.getId());
        return ApiResponse.success(ItineraryResponse.ItineraryScheduleResponse.from(itinerarySchedule, location));
    }

    @PostMapping("")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ItineraryResponse.ItineraryScheduleResponse>> store(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long itinerary,
            @PathVariable Long itineraryDay,
            @RequestBody ItineraryTourCreateRequest request
    ) {
        ItinerarySchedule itinerarySchedule = itineraryScheduleService.store(itinerary, itineraryDay, userDetails.getId(), request);
        return ApiResponse.success(ItineraryResponse.ItineraryScheduleResponse.from(itinerarySchedule));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ItineraryResponse.ItineraryScheduleResponse>> updateTourItem(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long itinerary,
            @PathVariable Long itineraryDay,
            @PathVariable Long id,
            @RequestBody ItineraryTourUpdateRequest request
    ) {
        ItinerarySchedule itinerarySchedule = itineraryScheduleService.updateTourItem(itinerary, itineraryDay, id, userDetails.getId(), request);
        return ApiResponse.success(ItineraryResponse.ItineraryScheduleResponse.from(itinerarySchedule));
    }
}


