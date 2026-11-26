package com.bosshi.maeul.auth.oidc;

/**
 * 검증한 ID 토큰의 사용자
 *
 * @param subject  그 로그인 수단의 사용자 ID (카카오 회원번호, Apple sub)
 * @param nickname 토큰에 닉네임이 있으면 그 값 (카카오), 없으면 null
 */
public record VerifiedIdentity(IdTokenProvider provider, String subject, String nickname) {
}
