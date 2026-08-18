package com.bosshi.maeul.post.repositories;

import com.bosshi.maeul.post.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
}
