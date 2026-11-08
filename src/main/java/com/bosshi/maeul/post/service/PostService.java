package com.bosshi.maeul.post.service;

import com.bosshi.maeul.post.entity.Post;
import com.bosshi.maeul.post.repository.PostRepository;
import com.bosshi.maeul.post.request.PostCreateRequest;
import com.bosshi.maeul.post.request.PostUpdateRequest;
import com.bosshi.maeul.post.type.PostCategory;
import com.bosshi.maeul.user.entity.User;
import com.bosshi.maeul.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public Post createPost(Long userId, PostCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Post post = new Post();
        post.setUser(user);
        post.setCategory(PostCategory.valueOf(request.category().toUpperCase()));
        post.setTitle(request.title());
        post.setContent(request.content());

        return postRepository.save(post);
    }

    public List<Post> getPosts() {
        return postRepository.findAll();
    }

    public Post getPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        post.setViewCount(post.getViewCount() + 1);
        return postRepository.save(post);
    }

    public Post updatePost(Long postId, PostUpdateRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        post.setTitle(request.title());
        post.setContent(request.content());
        post.setUpdatedAt(LocalDateTime.now());

        return postRepository.save(post);
    }

    public void deletePost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        postRepository.delete(post);
    }
}
