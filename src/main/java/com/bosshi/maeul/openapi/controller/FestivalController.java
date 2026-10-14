package com.bosshi.maeul.openapi.controller;

import com.bosshi.maeul.common.response.ApiResponse;
import com.bosshi.maeul.openapi.entity.Tour;
import com.bosshi.maeul.openapi.request.SearchFestivalRequest;
import com.bosshi.maeul.openapi.response.FestivalCategoryResponse;
import com.bosshi.maeul.openapi.service.FestivalService;
import com.bosshi.maeul.openapi.service.TourCategoryService;
import com.bosshi.maeul.openapi.service.TourSyncService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/festivals")
@RequiredArgsConstructor
public class FestivalController {
    private final FestivalService festivalService;
    private final TourCategoryService tourCategoryService;
    private final TourSyncService tourSyncService;

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<FestivalCategoryResponse>>> categories() {
        List<FestivalCategoryResponse> responses = tourCategoryService.festivalCategories()
                .stream()
                .map(tourCategory -> new FestivalCategoryResponse(tourCategory.getId(), tourCategory.getName()))
                .toList();
        return ApiResponse.success(responses);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<Tour>>> index(@Valid SearchFestivalRequest request, @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<Tour> tours = festivalService.search(request, pageable);
        return ApiResponse.success(tours);
    }

    @GetMapping("/popular")
    public ResponseEntity<ApiResponse<List<Tour>>> popular() {
        List<Tour> tours = festivalService.popular();
        return ApiResponse.success(tours);
    }

    @GetMapping("/{contentId}")
    public ResponseEntity<ApiResponse<Tour>> show(@PathVariable String contentId) {
        Tour tour = festivalService.findByContentId(contentId);

        try {
            tour = tourSyncService.syncDetailCommon(tour);
        } catch (Exception e) {
            log.error("Failed to sync tour data for contentId {}: {}", contentId, e.getMessage());
        }

        try {
            tour = tourSyncService.syncDetailIntro(tour);
        } catch (Exception e) {
            log.error("Failed to sync tour data for contentId {}: {}", contentId, e.getMessage());
        }
        return ApiResponse.success(tour);
    }
}
