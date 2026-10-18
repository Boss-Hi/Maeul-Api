package com.bosshi.maeul.itinerary.controller;

import com.bosshi.maeul.auth.CustomUserDetails;
import com.bosshi.maeul.common.response.ApiResponse;
import com.bosshi.maeul.itinerary.entity.ItineraryDay;
import com.bosshi.maeul.itinerary.response.ItineraryResponse;
import com.bosshi.maeul.itinerary.service.ItineraryDayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/itinerary/{itinerary}/days")
@RequiredArgsConstructor
@Slf4j
public class ItineraryDayController {
    private final ItineraryDayService itineraryDayService;

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ItineraryResponse.ItineraryDayResponse>> show(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long itinerary,
            @PathVariable Long id
    ) {
        ItineraryDay itineraryDay = itineraryDayService.show(itinerary, id, userDetails.getId());
        return ApiResponse.success(ItineraryResponse.ItineraryDayResponse.from(itineraryDay));
    }

    /*@PostMapping("/{id}/days")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ItineraryResponse>> addDay(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @RequestBody ItineraryDayCreateRequest request
    ) {
        Itinerary itinerary = itineraryDayService.addDay(id, userDetails.getId(), request);
        return ApiResponse.success(ItineraryResponse.from(itinerary));
    }*/

    /*@PutMapping("/{id}/days/{dayId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ItineraryResponse>> updateDay(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @PathVariable Long dayId,
            @RequestBody ItineraryDayUpdateRequest request
    ) {
        Itinerary itinerary = itineraryService.updateDay(id, dayId, userDetails.getId(), request);
        return ApiResponse.success(ItineraryResponse.from(itinerary));
    }*/

}


