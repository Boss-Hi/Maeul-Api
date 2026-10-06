package com.bosshi.maeul.user.entity;

import com.bosshi.maeul.common.entity.BaseEntity;
import com.bosshi.maeul.user.type.UserRole;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.modulith.NamedInterface;


@Builder
@NamedInterface
@Entity
@Table(name = "users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(length = 32)
    private String authType;

    @Column(length = 256)
    @Builder.Default
    private String authId = "";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role = UserRole.USER;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Profile profile;
}
