package com.bosshi.maeul.openapi.controller;

import com.bosshi.maeul.common.response.ApiResponse;
import com.bosshi.maeul.openapi.request.SearchFestivalRequest;
import com.bosshi.maeul.openapi.service.FestivalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/festivals")
@RequiredArgsConstructor
public class FestivalController {
    private final FestivalService festivalService;

    @GetMapping
    public ResponseEntity<?> index(SearchFestivalRequest request) {
        return ApiResponse.success(festivalService.search(request));
    }
}
