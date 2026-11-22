package com.bosshi.maeul.auth.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 앱 로그인: 네이티브 SDK 로 받은 OIDC ID 토큰
 *
 * @param nonce    로그인 요청에 쓴 nonce 원문
 * @param nickname 처음 가입할 때 쓸 닉네임 (Apple 은 토큰에 이름이 없어 앱이 첫 로그인 때 받은 이름을 보낸다). 없어도 된다
 */
public record IdTokenLoginRequest(@NotBlank String idToken, @NotBlank String nonce, String nickname) {
}
