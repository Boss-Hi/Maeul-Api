package com.bosshi.maeul.timeline.controller;

import com.bosshi.maeul.auth.CustomUserDetails;
import com.bosshi.maeul.common.response.ApiResponse;
import com.bosshi.maeul.timeline.config.TimelineProperties;
import com.bosshi.maeul.timeline.request.TimelinePointsRequest;
import com.bosshi.maeul.timeline.response.TimelineRecordResponse;
import com.bosshi.maeul.timeline.response.TimelineResponse;
import com.bosshi.maeul.timeline.service.TimelineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * 위치 기록 타임라인. 로그인 사용자 본인의 기록만 다룬다.
 */
@RestController
@RequestMapping("/api/timeline")
@RequiredArgsConstructor
public class TimelineController {
    private final TimelineService timelineService;
    private final TimelineProperties timelineProperties;

    /**
     * 위치 기록 업로드
     */
    @PostMapping("/points")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<TimelineRecordResponse>> record(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody TimelinePointsRequest request
    ) {
        return ApiResponse.success(timelineService.record(userDetails.getId(), request));
    }

    /**
     * 하루 타임라인 조회
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<TimelineResponse>> show(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ApiResponse.success(timelineService.getDay(userDetails.getId(), date));
    }

}
