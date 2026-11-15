package com.bosshi.maeul.territory.repository;

import com.bosshi.maeul.territory.entity.FatLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FatLevelRepository extends JpaRepository<FatLevel, Long> {

    List<FatLevel> findByDeletedAtIsNullOrderByLevelAsc();
}
