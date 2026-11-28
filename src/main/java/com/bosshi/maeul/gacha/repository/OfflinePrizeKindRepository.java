package com.bosshi.maeul.gacha.repository;

import com.bosshi.maeul.gacha.entity.OfflinePrizeKind;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OfflinePrizeKindRepository extends JpaRepository<OfflinePrizeKind, Long> {
    java.util.List<OfflinePrizeKind> findByDeletedAtIsNullOrderByIdAsc();

    /** 지워진 행도 센다 */
    boolean existsByName(String name);

    java.util.List<OfflinePrizeKind> findByIdIn(java.util.Collection<Long> ids);
}
