package com.bosshi.maeul.openapi.controller;

import com.bosshi.maeul.openapi.response.RegionResponse;
import com.bosshi.maeul.openapi.service.RegionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
}