package com.bosshi.maeul.openapi.repository;

import com.bosshi.maeul.openapi.entity.LDongRegion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.modulith.NamedInterface;

@NamedInterface
public interface LDongRegionRepository extends JpaRepository<LDongRegion, String>, JpaSpecificationExecutor<LDongRegion> {
}
