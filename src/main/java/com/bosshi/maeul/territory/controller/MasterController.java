package com.bosshi.maeul.territory.controller;

import com.bosshi.maeul.auth.CustomUserDetails;
import com.bosshi.maeul.common.response.ApiResponse;
import com.bosshi.maeul.territory.response.MasterRankingResponse;
import com.bosshi.maeul.territory.response.MasterResponse;
import com.bosshi.maeul.territory.service.FatRankingService;
import com.bosshi.maeul.territory.service.MasterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 주인: 병아리를 키우는 사용자의 성장 기록 (모든 병아리 경험치 합 x 주인 비율, 별도 레벨표)
 */
@RestController
@RequestMapping("/api/master")
@RequiredArgsConstructor
public class MasterController {
    private final MasterService masterService;
    private final FatRankingService rankingService;

    /**
     * 내 주인의 레벨과 경험치
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<MasterResponse>> me(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.success(MasterResponse.from(masterService.master(userDetails.getId())));
    }

    /**
     * 지역별 주인 순위. regionCode + code 는 시군구, regionCode 만 있으면 시도, 둘 다 없으면 전국
     */
    @GetMapping("/ranking")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<MasterRankingResponse>> ranking(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) String regionCode,
            @RequestParam(required = false) String code,
            @RequestParam(defaultValue = "50") int size
    ) {
        return ApiResponse.success(MasterRankingResponse.from(rankingService.masterRanking(userDetails.getId(), regionCode, code, size)));
    }
}
