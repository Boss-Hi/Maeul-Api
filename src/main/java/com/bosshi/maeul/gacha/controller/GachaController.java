package com.bosshi.maeul.gacha.controller;

import com.bosshi.maeul.auth.CustomUserDetails;
import com.bosshi.maeul.common.response.ApiResponse;
import com.bosshi.maeul.gacha.GachaType;
import com.bosshi.maeul.gacha.response.GachaOddsResponse;
import com.bosshi.maeul.gacha.response.GachaResultResponse;
import com.bosshi.maeul.gacha.response.OwnedItemResponse;
import com.bosshi.maeul.gacha.response.VoucherResponse;
import com.bosshi.maeul.gacha.service.GachaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 가챠: 졸업, 일정 완료, 미션 완료 때 서버가 바로 뽑은 결과를 조회한다 (ADR 0014)
 */
@RestController
@RequestMapping("/api/gacha")
@RequiredArgsConstructor
public class GachaController {
    private final GachaService gachaService;

    /**
     * 내 가챠 결과 (최근 순). page(0부터), size(기본 20)
     */
    @GetMapping("/results")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<GachaResultResponse>>> results(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ApiResponse.success(gachaService.results(userDetails.getId(), pageable).map(GachaResultResponse::from));
    }

    /**
     * 아직 확인하지 않은 가챠 결과 (뽑은 순). 앱은 이것으로 뽑기 연출을 보여 준다
     */
    @GetMapping("/results/unseen")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<GachaResultResponse>>> unseen(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.success(gachaService.unseen(userDetails.getId()).stream().map(GachaResultResponse::from).toList());
    }

    /**
     * 가챠 결과를 확인함으로 표시
     */
    @PostMapping("/results/{resultId}/seen")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<GachaResultResponse>> seen(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long resultId
    ) {
        return ApiResponse.success(GachaResultResponse.from(gachaService.markSeen(userDetails.getId(), resultId)));
    }

    /**
     * 가챠의 지금 확률 (공개)
     */
    @GetMapping("/{gachaType}/odds")
    public ResponseEntity<ApiResponse<GachaOddsResponse>> odds(@PathVariable GachaType gachaType) {
        return ApiResponse.success(GachaOddsResponse.from(gachaService.odds(gachaType)));
    }

    /**
     * 내가 가진 아이템과 수량
     */
    @GetMapping("/items")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<OwnedItemResponse>>> items(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.success(gachaService.items(userDetails.getId()).stream().map(OwnedItemResponse::from).toList());
    }

    /**
     * 내 상품 교환권 (최근 순)
     */
    @GetMapping("/vouchers")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<VoucherResponse>>> vouchers(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.success(gachaService.vouchers(userDetails.getId()).stream().map(VoucherResponse::from).toList());
    }
}
