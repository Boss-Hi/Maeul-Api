package com.bosshi.maeul.territory.controller;

import com.bosshi.maeul.auth.CustomUserDetails;
import com.bosshi.maeul.common.response.ApiResponse;
import com.bosshi.maeul.territory.response.MyTerritoryResponse;
import com.bosshi.maeul.territory.response.TerritoryRankingResponse;
import com.bosshi.maeul.territory.response.TerritoryRegionResponse;
import com.bosshi.maeul.territory.service.TerritoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

/**
 * 점령: 위치 기록으로 쌓은 시군구별 점령 시간과 월간 순위
 */
@RestController
@RequestMapping("/api/territory")
@RequiredArgsConstructor
public class TerritoryController {
    private final TerritoryService territoryService;

    /**
     * 내 점령 현황 (현재 방문, 시군구별 누적/월간 순위, 포인트 적립 진행)
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<MyTerritoryResponse>> me(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month
    ) {
        return ApiResponse.success(MyTerritoryResponse.from(
                territoryService.myTerritory(userDetails.getId(), month == null ? territoryService.currentMonth() : month)));
    }

    /**
     * 지역 목록 (전국 / 시도 / 시군구)
     */
    @GetMapping("/regions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<TerritoryRegionResponse>>> regions(@RequestParam(required = false) String parentRegionId) {
        return ApiResponse.success(territoryService.regions(parentRegionId).stream().map(TerritoryRegionResponse::from).toList());
    }

    /**
     * 시군구 월간 점령 순위
     */
    @GetMapping("/regions/{regionId}/ranking")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<TerritoryRankingResponse>> ranking(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String regionId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month,
            @RequestParam(defaultValue = "50") int size
    ) {
        return ApiResponse.success(TerritoryRankingResponse.from(territoryService.ranking(userDetails.getId(), regionId,
                month == null ? territoryService.currentMonth() : month, size)));
    }
}
