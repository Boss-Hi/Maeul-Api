package com.bosshi.maeul.post.api;

import java.time.LocalDateTime;

public record PostResponse(
        Long id,
        Long userId,
        String category,
        String title,
        String content,
        Long viewCount,
        LocalDateTime createdAt
) {
}
