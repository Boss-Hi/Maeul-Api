package com.bosshi.maeul.territory.repository;

import com.bosshi.maeul.territory.entity.TerritoryRegionCell;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface TerritoryRegionCellRepository extends JpaRepository<TerritoryRegionCell, String> {

    /** 시군구별 이름 1건씩 (캐시된 카카오 이름) */
    @Query("select c from TerritoryRegionCell c where c.regionId in :regionIds")
    List<TerritoryRegionCell> findByRegionIdIn(@Param("regionIds") Collection<String> regionIds);
}
