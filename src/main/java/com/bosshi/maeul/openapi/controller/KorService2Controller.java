package com.bosshi.maeul.openapi.controller;

import com.bosshi.maeul.openapi.request.SearchFestivalRequest;
import com.bosshi.maeul.openapi.service.OpenApiService;
import com.bosshi.maeul.openapi.type.TourApiEndpoint;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/open")
@RequiredArgsConstructor
public class KorService2Controller {
    private final OpenApiService openApiService;

    @GetMapping("/location-based-list")
    public ResponseEntity<String> getLocationBasedList(@RequestParam MultiValueMap<String, String> params) {
        return ResponseEntity.ok(openApiService.call(TourApiEndpoint.LOCATION_BASED_LIST, params));
    }

    @GetMapping("/search-keyword")
    public ResponseEntity<String> searchKeyword(@RequestParam MultiValueMap<String, String> params) {
        return ResponseEntity.ok(openApiService.call(TourApiEndpoint.SEARCH_KEYWORD, params));
    }

    @GetMapping("/search-festival")
    public ResponseEntity<?> searchFestival(@ModelAttribute SearchFestivalRequest request) {
        if (!StringUtils.hasText(request.getEventStartDate()) || !StringUtils.hasText(request.getEventEndDate())) {
            return ResponseEntity.badRequest().body("eventStartDate and eventEndDate are required.");
        }

        return ResponseEntity.ok(openApiService.searchFestival(request));
    }

    @GetMapping("/search-stay")
    public ResponseEntity<String> searchStay(@RequestParam MultiValueMap<String, String> params) {
        return ResponseEntity.ok(openApiService.call(TourApiEndpoint.SEARCH_STAY, params));
    }

    @GetMapping("/detail-common")
    public ResponseEntity<String> getDetailCommon(@RequestParam MultiValueMap<String, String> params) {
        return ResponseEntity.ok(openApiService.call(TourApiEndpoint.DETAIL_COMMON, params));
    }

    @GetMapping("/detail-intro")
    public ResponseEntity<String> getDetailIntro(@RequestParam MultiValueMap<String, String> params) {
        return ResponseEntity.ok(openApiService.call(TourApiEndpoint.DETAIL_INTRO, params));
    }

    @GetMapping("/detail-info")
    public ResponseEntity<String> getDetailInfo(@RequestParam MultiValueMap<String, String> params) {
        return ResponseEntity.ok(openApiService.call(TourApiEndpoint.DETAIL_INFO, params));
    }

    @GetMapping("/detail-image")
    public ResponseEntity<String> getDetailImage(@RequestParam MultiValueMap<String, String> params) {
        return ResponseEntity.ok(openApiService.call(TourApiEndpoint.DETAIL_IMAGE, params));
    }

    @GetMapping("/area-based-sync-list")
    public ResponseEntity<String> getAreaBasedSyncList(@RequestParam MultiValueMap<String, String> params) {
        return ResponseEntity.ok(openApiService.call(TourApiEndpoint.AREA_BASED_SYNC_LIST, params));
    }

    @GetMapping("/detail-pet-tour")
    public ResponseEntity<String> getDetailPetTour(@RequestParam MultiValueMap<String, String> params) {
        return ResponseEntity.ok(openApiService.call(TourApiEndpoint.DETAIL_PET_TOUR, params));
    }

    @GetMapping("/area-based-list")
    public ResponseEntity<String> getAreaBasedList(@RequestParam MultiValueMap<String, String> params) {
        return ResponseEntity.ok(openApiService.call(TourApiEndpoint.AREA_BASED_LIST, params));
    }

    @GetMapping("/ldong-code")
    public ResponseEntity<String> getLdongCode(@RequestParam MultiValueMap<String, String> params) {
        return ResponseEntity.ok(openApiService.call(TourApiEndpoint.LDONG_CODE, params));
    }

    @GetMapping("/lcls-systm-code")
    public ResponseEntity<String> getLclsSystemCode(@RequestParam MultiValueMap<String, String> params) {
        return ResponseEntity.ok(openApiService.call(TourApiEndpoint.LCLS_SYSTEM_CODE, params));
    }
}
