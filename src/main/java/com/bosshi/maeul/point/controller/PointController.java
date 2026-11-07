package com.bosshi.maeul.point.controller;

import com.bosshi.maeul.auth.CustomUserDetails;
import com.bosshi.maeul.common.response.ApiResponse;
import com.bosshi.maeul.point.response.PointResponse;
import com.bosshi.maeul.point.service.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/points")
@RequiredArgsConstructor
public class PointController {
    private static final int MAX_SIZE = 100;

    private final PointService pointService;

    /**
     * 포인트 잔액과 최근 내역
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PointResponse>> show(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "20") int size
    ) {
        Long userId = userDetails.getId();
        return ApiResponse.success(new PointResponse(
                pointService.balance(userId),
                pointService.recent(userId, Math.clamp(size, 1, MAX_SIZE)).stream().map(PointResponse.Transaction::from).toList()));
    }
}
