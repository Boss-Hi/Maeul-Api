package com.bosshi.maeul.user.requests;

public record RegisterRequest(
        String email,
        String password,
        String nickname
) {
}
