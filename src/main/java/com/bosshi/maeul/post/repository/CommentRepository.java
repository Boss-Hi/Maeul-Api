package com.bosshi.maeul.post.repository;

import com.bosshi.maeul.post.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}
