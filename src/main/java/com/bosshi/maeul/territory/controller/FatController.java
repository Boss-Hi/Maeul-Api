package com.bosshi.maeul.territory.controller;

import com.bosshi.maeul.auth.CustomUserDetails;
import com.bosshi.maeul.common.response.ApiResponse;
import com.bosshi.maeul.territory.request.AdoptFatRequest;
import com.bosshi.maeul.territory.response.FatOverviewResponse;
import com.bosshi.maeul.territory.response.FatRankingResponse;
import com.bosshi.maeul.territory.response.FatRegionResponse;
import com.bosshi.maeul.territory.response.FatResponse;
import com.bosshi.maeul.territory.response.GraduatedFatResponse;
import com.bosshi.maeul.territory.service.FatRankingService;
import com.bosshi.maeul.territory.service.FatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 병아리: 전국 어느 시군구에서나 미리 분양받아 키우고, 건국 레벨부터 상품을 교환해 졸업한다
 */
@RestController
@RequestMapping("/api/fats")
@RequiredArgsConstructor
public class FatController {
    private final FatService fatService;
    private final FatRankingService rankingService;

    /**
     * 키우는 중인 병아리와 지금 분양받을 수 있는 시군구
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<FatOverviewResponse>> overview(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.success(FatOverviewResponse.from(fatService.overview(userDetails.getId())));
    }

    /**
     * 병아리를 분양받을 수 있는 시군구 전체 (region_subdivisions 중 점령 시간이 쌓이는 곳)
     */
    @GetMapping("/regions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<FatRegionResponse>>> regions() {
        return ApiResponse.success(fatService.regions().stream().map(FatRegionResponse::from).toList());
    }

    /**
     * 지역별 병아리 순위. regionCode + code 는 시군구, regionCode 만 있으면 시도, 둘 다 없으면 전국
     */
    @GetMapping("/ranking")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<FatRankingResponse>> ranking(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) String regionCode,
            @RequestParam(required = false) String code,
            @RequestParam(defaultValue = "50") int size
    ) {
        return ApiResponse.success(FatRankingResponse.from(rankingService.fatRanking(userDetails.getId(), regionCode, code, size)));
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
        return ApiResponse.success(FatResponse.from(fatService.adopt(userDetails.getId(), request.regionCode(), request.code())));
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
