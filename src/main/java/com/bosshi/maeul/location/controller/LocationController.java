package com.bosshi.maeul.location.controller;

import com.bosshi.maeul.common.response.ApiResponse;
import com.bosshi.maeul.location.dto.LocationDTO;
import com.bosshi.maeul.location.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/location")
public class LocationController {
    private final LocationService locationService;

    @GetMapping()
    public ResponseEntity<ApiResponse<LocationDTO>> getAddress(@RequestParam Double latitude, @RequestParam Double longitude) {
        LocationDTO dto = locationService.getRegionAddress(latitude, longitude);
        return ApiResponse.success(dto);
    }
}
