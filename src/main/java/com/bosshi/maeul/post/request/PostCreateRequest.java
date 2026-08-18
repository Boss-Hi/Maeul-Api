package com.bosshi.maeul.post.request;

public record PostCreateRequest(
        String category,
        String title,
        String content
) {
}
