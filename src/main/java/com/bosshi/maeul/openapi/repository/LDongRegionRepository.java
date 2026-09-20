package com.bosshi.maeul.openapi.repository;

import com.bosshi.maeul.openapi.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.modulith.NamedInterface;

import java.util.List;

@NamedInterface
public interface LDongRegionRepository extends JpaRepository<Region, String>, JpaSpecificationExecutor<Region> {

    // N+1 쿼리 방지를 위한 Fetch Join
    @Query("SELECT DISTINCT r FROM Region r LEFT JOIN FETCH r.sigungus")
    List<Region> findAllWithSigungus();
}
