package com.bosshi.maeul.territory.repository;

import com.bosshi.maeul.territory.entity.ChickLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChickLevelRepository extends JpaRepository<ChickLevel, Long> {

    List<ChickLevel> findByDeletedAtIsNullOrderByLevelAsc();
}
