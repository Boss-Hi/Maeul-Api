package com.bosshi.maeul.openapi.repository;

import com.bosshi.maeul.openapi.entity.Festival;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.modulith.NamedInterface;

@NamedInterface
public interface FestivalRepository extends JpaRepository<Festival, String>, JpaSpecificationExecutor<Festival> {
}
