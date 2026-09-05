package com.bosshi.maeul.user.controller;

import com.bosshi.maeul.common.response.ApiResponse;
import com.bosshi.maeul.user.request.LoginRequest;
import com.bosshi.maeul.user.request.RegisterRequest;
import com.bosshi.maeul.user.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Boolean>> register(@RequestBody RegisterRequest request) {
        authService.register(request);
        return ApiResponse.success(true);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> login(@RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }
}
