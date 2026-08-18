package com.bosshi.maeul.post.requests;

public record PostCreateRequest(
        String category,
        String title,
        String content
) {
}
