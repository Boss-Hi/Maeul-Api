package com.bosshi.maeul.user.api;

public record LoginRequest(
        String email,
        String password
) {
}
