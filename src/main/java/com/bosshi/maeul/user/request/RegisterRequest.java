package com.bosshi.maeul.user.request;

public record RegisterRequest(
        String email,
        String password,
        String nickname
) {
}
