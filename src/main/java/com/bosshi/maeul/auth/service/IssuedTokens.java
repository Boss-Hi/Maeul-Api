package com.bosshi.maeul.auth.service;

import org.springframework.modulith.NamedInterface;

import java.time.LocalDateTime;

/**
 * 로그인이나 갱신으로 발급한 토큰 한 쌍 (ADR 0015)
 *
 * @param accessTokenExpiresIn 액세스 토큰 수명(초)
 */
@NamedInterface
public record IssuedTokens(String accessToken, long accessTokenExpiresIn, String refreshToken, LocalDateTime refreshTokenExpiresAt) {
}
