package com.bosshi.maeul.user.request;

public record LoginRequest(
        String email,
        String password
) {
}
