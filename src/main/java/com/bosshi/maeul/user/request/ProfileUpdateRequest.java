package com.bosshi.maeul.user.request;

public record ProfileUpdateRequest(
        String name,
        String nickname,
        String description,
        String avatarUrl,
        Integer year,
        Integer month,
        Integer day
) {
}

