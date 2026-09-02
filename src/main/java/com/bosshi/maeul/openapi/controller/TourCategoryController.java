package com.bosshi.maeul.openapi.controller;

import com.bosshi.maeul.common.response.ApiResponse;
import com.bosshi.maeul.openapi.response.TourCategoryTreeResponse;
import com.bosshi.maeul.openapi.service.TourCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tour-categories")
@RequiredArgsConstructor
public class TourCategoryController {
    private final TourCategoryService tourCategoryService;

    @GetMapping
    public ResponseEntity<?> index() {
        return ApiResponse.success(TourCategoryTreeResponse.buildTree(tourCategoryService.all()));
    }
}
