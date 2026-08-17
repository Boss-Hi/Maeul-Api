package com.bosshi.maeul.api.post;

import java.time.LocalDateTime;

public record PostResponse(
        Long id,
        Long userId,
        Long neighborhoodId,
        String category,
        String title,
        String content,
        Long viewCount,
        LocalDateTime createdAt
) {
}
