package com.bosshi.maeul.itineraries.itinerary.photomission;

import com.bosshi.maeul.auth.CustomUserDetails;
import com.bosshi.maeul.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

/** 사진 미션: 기준 사진 퀴즈를 보고 같은 곳을 찍어 올린다 (ADR 0017) */
@RestController
@RequestMapping("/api/photo-missions")
@RequiredArgsConstructor
public class PhotoMissionController {

    private final PhotoMissionService photoMissionService;

    /** 진행 중인 일정의 사진 미션. 없으면 빈 목록 */
    @GetMapping("/current")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<PhotoMissionResponse>>> current(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.success(photoMissionService.current(userDetails.getId()).stream().map(PhotoMissionResponse::from).toList());
    }

    /** 서버가 서빙하는 기준 사진 이미지. 퀴즈 화면의 <img src> 로 바로 쓸 수 있도록 인증 없이 열려 있다 */
    @GetMapping("/reference-photos/{id}/image")
    public ResponseEntity<byte[]> referenceImage(@PathVariable Long id) {
        return photoMissionService.referenceImage(id)
                .map(image -> ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(image.mimeType()))
                        .cacheControl(CacheControl.maxAge(Duration.ofDays(1)).cachePublic())
                        .body(image.data()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /** 사진(photo, JPEG/PNG/WebP 5MB 이하)과 현재 위치(lat, lng)를 올린다. 장소 반경 안에서만 받는다 */
    @PostMapping(value = "/{id}/submit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PhotoMissionSubmitResponse>> submit(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @RequestParam("photo") MultipartFile photo,
            @RequestParam double lat,
            @RequestParam double lng
    ) throws IOException {
        return ApiResponse.success(PhotoMissionSubmitResponse.from(
                photoMissionService.submit(userDetails.getId(), id, photo.getBytes(), lat, lng)));
    }
}
