package com.bosshi.maeul.gacha.repository;

import com.bosshi.maeul.gacha.entity.OfflineGachaLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OfflineGachaLogRepository extends JpaRepository<OfflineGachaLog, Long> {
    org.springframework.data.domain.Page<OfflineGachaLog> findByStoreIdOrderByIdDesc(Long storeId, org.springframework.data.domain.Pageable pageable);
}
