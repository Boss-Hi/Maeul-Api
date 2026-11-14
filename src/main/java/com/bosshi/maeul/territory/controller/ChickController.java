package com.bosshi.maeul.territory.controller;

import com.bosshi.maeul.auth.CustomUserDetails;
import com.bosshi.maeul.common.response.ApiResponse;
import com.bosshi.maeul.territory.request.AdoptChickRequest;
import com.bosshi.maeul.territory.response.ChickOverviewResponse;
import com.bosshi.maeul.territory.response.ChickResponse;
import com.bosshi.maeul.territory.response.GraduatedChickResponse;
import com.bosshi.maeul.territory.service.ChickService;
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
@RequestMapping("/api/chicks")
@RequiredArgsConstructor
public class ChickController {
    private final ChickService chickService;

    /**
     * 키우는 중인 병아리와 지금 분양받을 수 있는 시군구
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ChickOverviewResponse>> overview(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.success(ChickOverviewResponse.from(chickService.overview(userDetails.getId())));
    }

    /**
     * 졸업한 병아리 (최근 졸업 순)
     */
    @GetMapping("/graduated")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<GraduatedChickResponse>>> graduated(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.success(chickService.graduated(userDetails.getId()).stream().map(GraduatedChickResponse::from).toList());
    }

    /**
     * 병아리 분양
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ChickResponse>> adopt(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AdoptChickRequest request
    ) {
        return ApiResponse.success(ChickResponse.from(chickService.adopt(userDetails.getId(), request.regionId())));
    }

    /**
     * 졸업 (상품 교환)
     */
    @PostMapping("/{chickId}/graduate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<GraduatedChickResponse>> graduate(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long chickId
    ) {
        return ApiResponse.success(GraduatedChickResponse.from(chickService.graduate(userDetails.getId(), chickId)));
    }
}
