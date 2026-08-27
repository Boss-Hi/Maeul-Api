package com.bosshi.maeul.post.repository;

import com.bosshi.maeul.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
}
