package com.bosshi.maeul.gacha.repository;

import com.bosshi.maeul.gacha.entity.OfflinePrizeKind;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OfflinePrizeKindRepository extends JpaRepository<OfflinePrizeKind, Long> {
    java.util.List<OfflinePrizeKind> findByDeletedAtIsNullOrderByIdAsc();

    java.util.List<OfflinePrizeKind> findByIdIn(java.util.Collection<Long> ids);
}
