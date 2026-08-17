package com.bosshi.maeul.domain.follow;

import com.bosshi.maeul.domain.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "follows")
@Getter
@Setter
public class Follow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "follower_id", nullable = false)
    private User follower;

    @ManyToOne
    @JoinColumn(name = "followee_id", nullable = false)
    private User followee;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
