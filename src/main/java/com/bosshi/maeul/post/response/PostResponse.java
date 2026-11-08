package com.bosshi.maeul.post.response;

import com.bosshi.maeul.post.entity.Post;

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
    public static PostResponse from(Post post) {
        return new PostResponse(
                post.getId(),
                post.getUser().getId(),
                post.getCategory().name(),
                post.getTitle(),
                post.getContent(),
                post.getViewCount(),
                post.getCreatedAt()
        );
    }
}
