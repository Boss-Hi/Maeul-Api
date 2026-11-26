package com.bosshi.maeul.gacha.repository;

import com.bosshi.maeul.gacha.GachaType;
import com.bosshi.maeul.gacha.entity.GachaResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GachaResultRepository extends JpaRepository<GachaResult, Long> {

    Optional<GachaResult> findByUserIdAndGachaTypeAndRefKey(Long userId, GachaType gachaType, String refKey);

    Page<GachaResult> findByUserIdOrderByIdDesc(Long userId, Pageable pageable);

    List<GachaResult> findByUserIdAndSeenAtIsNullOrderByIdAsc(Long userId);

    Optional<GachaResult> findByIdAndUserId(Long id, Long userId);
}
