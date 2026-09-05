package com.bosshi.maeul.openapi.repository;

import com.bosshi.maeul.openapi.entity.LDongSigungu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.modulith.NamedInterface;

@NamedInterface
public interface LDongSigunguRepository extends JpaRepository<LDongSigungu, String>, JpaSpecificationExecutor<LDongSigungu> {
}
