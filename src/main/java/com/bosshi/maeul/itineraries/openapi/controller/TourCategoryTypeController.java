package com.bosshi.maeul.itineraries.openapi.controller;

import com.bosshi.maeul.itineraries.openapi.response.TourCategoryTypeResponse;
import com.bosshi.maeul.itineraries.openapi.service.TourCategoryTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tour-category-types")
@RequiredArgsConstructor
public class TourCategoryTypeController {
    private final TourCategoryTypeService tourCategoryTypeService;

    @GetMapping
    public ResponseEntity<?> index() {
        return ResponseEntity.ok(tourCategoryTypeService.all().stream().map(TourCategoryTypeResponse::from).toList());
    }
}
