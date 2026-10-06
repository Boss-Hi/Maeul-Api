package com.bosshi.maeul.user.repository;

import com.bosshi.maeul.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.modulith.NamedInterface;

import java.util.Optional;

@NamedInterface
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);

    User findByEmail(String email);
    Optional<User> findByAuthId(String authId);
    Optional<User> findByAuthTypeAndAuthId(String authType, String authId);
}
