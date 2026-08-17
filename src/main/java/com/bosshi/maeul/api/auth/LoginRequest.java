package com.bosshi.maeul.api.auth;

public record LoginRequest(
        String email,
        String password
) {
}
