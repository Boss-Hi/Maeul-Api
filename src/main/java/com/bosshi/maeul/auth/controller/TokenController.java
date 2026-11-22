package com.bosshi.maeul.auth.controller;

import com.bosshi.maeul.auth.config.AuthProperties;
import com.bosshi.maeul.auth.oidc.IdTokenProvider;
import com.bosshi.maeul.auth.request.IdTokenLoginRequest;
import com.bosshi.maeul.auth.request.LoginCodeRequest;
import com.bosshi.maeul.auth.request.RefreshTokenRequest;
import com.bosshi.maeul.auth.response.TokenResponse;
import com.bosshi.maeul.auth.service.AppLoginService;
import com.bosshi.maeul.auth.service.TokenService;
import com.bosshi.maeul.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * 앱 로그인, 토큰 갱신, 로그아웃, 웹 로그인 코드 교환, 카카오 연결 끊기 콜백 (ADR 0015)
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class TokenController {
    private final AppLoginService appLoginService;
    private final TokenService tokenService;
    private final AuthProperties properties;

    /**
     * 앱 로그인. provider 는 kakao 또는 apple. 네이티브 SDK 로 받은 ID 토큰과 nonce 원문을 보낸다
     */
    @PostMapping("/{provider}/token")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@PathVariable String provider, @Valid @RequestBody IdTokenLoginRequest request) {
        return ApiResponse.success(TokenResponse.from(
                appLoginService.login(IdTokenProvider.from(provider), request.idToken(), request.nonce(), request.nickname())));
    }

    /**
     * 리프레시 토큰을 새 토큰 한 쌍으로 바꾼다. 보낸 리프레시 토큰은 더 쓸 수 없다
     */
    @PostMapping("/token/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResponse.success(TokenResponse.from(tokenService.refresh(request.refreshToken())));
    }

    /**
     * 로그아웃: 이 기기의 리프레시 토큰을 폐기한다
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Boolean>> logout(@Valid @RequestBody RefreshTokenRequest request) {
        tokenService.logout(request.refreshToken());
        return ApiResponse.success(true);
    }

    /**
     * 웹 OAuth 로그인 뒤 리다이렉트 URL 의 1회용 code 를 토큰 한 쌍으로 바꾼다
     */
    @PostMapping("/code/exchange")
    public ResponseEntity<ApiResponse<TokenResponse>> exchange(@Valid @RequestBody LoginCodeRequest request) {
        return ApiResponse.success(TokenResponse.from(tokenService.exchangeLoginCode(request.code())));
    }

    /**
     * 카카오 연결 끊기 콜백. 카카오가 Authorization: KakaoAK {어드민 키} 와 user_id 를 보낸다
     */
    @RequestMapping(value = "/kakao/unlink", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<Void> kakaoUnlink(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam("user_id") String kakaoUserId
    ) {
        String adminKey = properties.getKakaoAdminKey();
        if (adminKey == null || adminKey.isBlank() || authorization == null
                || !MessageDigest.isEqual(("KakaoAK " + adminKey).getBytes(StandardCharsets.UTF_8), authorization.getBytes(StandardCharsets.UTF_8))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        appLoginService.kakaoUnlinked(kakaoUserId);
        return ResponseEntity.ok().build();
    }
}
