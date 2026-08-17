package com.bosshi.maeul.api.post;

public record PostCreateRequest(
        Long neighborhoodId,
        String category,
        String title,
        String content
) {
}
