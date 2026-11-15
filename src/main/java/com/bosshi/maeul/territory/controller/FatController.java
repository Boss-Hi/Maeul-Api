package com.bosshi.maeul.territory.controller;

import com.bosshi.maeul.auth.CustomUserDetails;
import com.bosshi.maeul.common.response.ApiResponse;
import com.bosshi.maeul.territory.request.AdoptFatRequest;
import com.bosshi.maeul.territory.response.FatOverviewResponse;
import com.bosshi.maeul.territory.response.FatResponse;
import com.bosshi.maeul.territory.response.GraduatedFatResponse;
import com.bosshi.maeul.territory.service.FatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 병아리: 시군구마다 점령 시간으로 키우고, 건국 레벨부터 상품을 교환해 졸업한다
 */
@RestController
@RequestMapping("/api/fats")
@RequiredArgsConstructor
public class FatController {
    private final FatService fatService;

    /**
     * 키우는 중인 병아리와 지금 분양받을 수 있는 시군구
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<FatOverviewResponse>> overview(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.success(FatOverviewResponse.from(fatService.overview(userDetails.getId())));
    }

    /**
     * 졸업한 병아리 (최근 졸업 순)
     */
    @GetMapping("/graduated")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<GraduatedFatResponse>>> graduated(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.success(fatService.graduated(userDetails.getId()).stream().map(GraduatedFatResponse::from).toList());
    }

    /**
     * 병아리 분양
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<FatResponse>> adopt(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AdoptFatRequest request
    ) {
        return ApiResponse.success(FatResponse.from(fatService.adopt(userDetails.getId(), request.regionId())));
    }

    /**
     * 졸업 (상품 교환)
     */
    @PostMapping("/{fatId}/graduate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<GraduatedFatResponse>> graduate(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long fatId
    ) {
        return ApiResponse.success(GraduatedFatResponse.from(fatService.graduate(userDetails.getId(), fatId)));
    }
}
