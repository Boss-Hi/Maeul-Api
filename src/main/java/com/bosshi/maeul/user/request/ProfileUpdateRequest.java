package com.bosshi.maeul.user.request;

public record ProfileUpdateRequest(
        String nickname,
        String bio,
        String avatarUrl
) {
}

