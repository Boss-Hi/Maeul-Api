package com.bosshi.maeul.api.auth;

public record RegisterRequest(
        String email,
        String password,
        String nickname
) {
}
