package com.bosshi.maeul.openapi.controller;

import com.bosshi.maeul.category.service.TourCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tour-category-type")
@RequiredArgsConstructor
public class TourCategoryTypeController {
    private final TourCategoryService tourCategoryService;

    @GetMapping({"/"})
    public ResponseEntity<?> index() {
        return ResponseEntity.ok(tourCategoryService.getAllTourCategories());
    }
}
