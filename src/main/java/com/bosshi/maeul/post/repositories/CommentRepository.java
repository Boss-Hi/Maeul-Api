package com.bosshi.maeul.post.repositories;

import com.bosshi.maeul.post.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}
