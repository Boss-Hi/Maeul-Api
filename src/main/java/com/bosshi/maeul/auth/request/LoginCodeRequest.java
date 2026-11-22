package com.bosshi.maeul.auth.request;

import jakarta.validation.constraints.NotBlank;

/** 웹 OAuth 로그인 뒤 리다이렉트 URL 의 code */
public record LoginCodeRequest(@NotBlank String code) {
}
