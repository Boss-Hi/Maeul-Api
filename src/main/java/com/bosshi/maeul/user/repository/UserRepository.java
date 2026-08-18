package com.bosshi.maeul.user.repository;

import com.bosshi.maeul.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.modulith.NamedInterface;

@NamedInterface
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);

    User findByEmail(String email);
}
