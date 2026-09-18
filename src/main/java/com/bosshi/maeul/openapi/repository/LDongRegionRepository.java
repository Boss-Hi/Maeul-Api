package com.bosshi.maeul.openapi.repository;

import com.bosshi.maeul.openapi.entity.LDongRegion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.modulith.NamedInterface;

import java.util.List;

@NamedInterface
public interface LDongRegionRepository extends JpaRepository<LDongRegion, String>, JpaSpecificationExecutor<LDongRegion> {

    // N+1 쿼리 방지를 위한 Fetch Join
    @Query("SELECT DISTINCT r FROM LDongRegion r LEFT JOIN FETCH r.sigungus")
    List<LDongRegion> findAllWithSigungus();
}
