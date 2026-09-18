package com.bosshi.maeul.openapi.controller;

import com.bosshi.maeul.auth.CustomUserDetails;
import com.bosshi.maeul.openapi.response.RegionResponse;
import com.bosshi.maeul.openapi.service.RegionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/regions")
@RequiredArgsConstructor
public class RegionController {
    private final RegionService regionService;

    @GetMapping
    public ResponseEntity<List<RegionResponse>> getRegions() {
        List<RegionResponse> regions = regionService.getRegionsWithSigungus().stream()
                .map(RegionResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(regions);
    }

    @GetMapping("/2")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<RegionResponse>> getRegions2(@AuthenticationPrincipal CustomUserDetails userDetails) {
        List<RegionResponse> regions = regionService.getRegionsWithSigungus().stream()
                .map(RegionResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(regions);
    }
}