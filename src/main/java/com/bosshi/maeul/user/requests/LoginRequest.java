package com.bosshi.maeul.user.requests;

public record LoginRequest(
        String email,
        String password
) {
}
