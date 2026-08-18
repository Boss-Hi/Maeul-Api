package com.bosshi.maeul.user.api;

public record RegisterRequest(
        String email,
        String password,
        String nickname
) {
}
