package com.bosshi.maeul.user.controller;

import com.bosshi.maeul.auth.CustomUserDetails;
import com.bosshi.maeul.common.response.ApiResponse;
import com.bosshi.maeul.user.entity.Profile;
import com.bosshi.maeul.user.request.ProfileUpdateRequest;
import com.bosshi.maeul.user.response.ProfileResponse;
import com.bosshi.maeul.user.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class ProfileController {
    private final ProfileService profileService;

    @GetMapping("/me/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ProfileResponse>> getMyProfile(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Profile profile = profileService.getMyProfile(userDetails.getId());
        return ApiResponse.success(ProfileResponse.from(profile));
    }

    @PutMapping("/me/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ProfileResponse>> updateMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody ProfileUpdateRequest request
    ) {
        Profile profile = profileService.updateMyProfile(userDetails.getId(), request);
        return ApiResponse.success(ProfileResponse.from(profile));
    }
}
