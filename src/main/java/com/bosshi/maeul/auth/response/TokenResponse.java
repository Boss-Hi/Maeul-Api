package com.bosshi.maeul.auth.response;

import com.bosshi.maeul.auth.service.IssuedTokens;
import org.springframework.modulith.NamedInterface;

import java.time.LocalDateTime;

/**
 * 로그인·갱신 결과. 카카오, Apple, 이메일 로그인 모두 같은 모양이다 (ADR 0015).
 *
 * @param accessTokenExpiresIn 액세스 토큰 수명(초)
 */
@NamedInterface
public record TokenResponse(String accessToken, String tokenType, long accessTokenExpiresIn,
                            String refreshToken, LocalDateTime refreshTokenExpiresAt) {

    public static TokenResponse from(IssuedTokens tokens) {
        return new TokenResponse(tokens.accessToken(), "Bearer", tokens.accessTokenExpiresIn(),
                tokens.refreshToken(), tokens.refreshTokenExpiresAt());
    }
}
