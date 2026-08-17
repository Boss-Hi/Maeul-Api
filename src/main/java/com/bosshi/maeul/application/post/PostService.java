package com.bosshi.maeul.application.post;

import com.bosshi.maeul.api.post.PostCreateRequest;
import com.bosshi.maeul.api.post.PostResponse;
import com.bosshi.maeul.api.post.PostUpdateRequest;
import com.bosshi.maeul.domain.neighborhood.Neighborhood;
import com.bosshi.maeul.domain.neighborhood.NeighborhoodRepository;
import com.bosshi.maeul.domain.post.Post;
import com.bosshi.maeul.domain.post.PostCategory;
import com.bosshi.maeul.domain.post.PostRepository;
import com.bosshi.maeul.domain.user.User;
import com.bosshi.maeul.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final NeighborhoodRepository neighborhoodRepository;

    public PostResponse createPost(Long userId, PostCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Neighborhood neighborhood = neighborhoodRepository.findById(request.neighborhoodId())
                .orElseThrow(() -> new IllegalArgumentException("동네를 찾을 수 없습니다."));

        Post post = new Post();
        post.setUser(user);
        post.setNeighborhood(neighborhood);
        post.setCategory(PostCategory.valueOf(request.category().toUpperCase()));
        post.setTitle(request.title());
        post.setContent(request.content());

        return toResponse(postRepository.save(post));
    }

    public List<PostResponse> getPosts() {
        return postRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public PostResponse getPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        post.setViewCount(post.getViewCount() + 1);
        return toResponse(postRepository.save(post));
    }

    public PostResponse updatePost(Long postId, PostUpdateRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        post.setTitle(request.title());
        post.setContent(request.content());
        post.setUpdatedAt(LocalDateTime.now());

        return toResponse(postRepository.save(post));
    }

    public void deletePost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        postRepository.delete(post);
    }

    private PostResponse toResponse(Post post) {
        return new PostResponse(
                post.getId(),
                post.getUser().getId(),
                post.getNeighborhood().getId(),
                post.getCategory().name(),
                post.getTitle(),
                post.getContent(),
                post.getViewCount(),
                post.getCreatedAt()
        );
    }
}
