package com.bosshi.maeul.openapi.repository;

import com.bosshi.maeul.openapi.entity.RegionSubdivisions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.modulith.NamedInterface;

@NamedInterface
public interface LDongSigunguRepository extends JpaRepository<RegionSubdivisions, String>, JpaSpecificationExecutor<RegionSubdivisions> {
}
