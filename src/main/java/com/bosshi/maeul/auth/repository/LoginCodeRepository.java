package com.bosshi.maeul.auth.repository;

import com.bosshi.maeul.auth.entity.LoginCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface LoginCodeRepository extends JpaRepository<LoginCode, Long> {

    Optional<LoginCode> findByCodeHash(String codeHash);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update LoginCode c set c.usedAt = :now where c.id = :id and c.usedAt is null")
    int markUsed(@Param("id") Long id, @Param("now") LocalDateTime now);
}
