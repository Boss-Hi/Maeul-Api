package com.bosshi.maeul.post.api;

public record PostCreateRequest(
        String category,
        String title,
        String content
) {
}
