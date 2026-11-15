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

    /** 카카오가 이 시도·시군구 이름으로 판정한 시군구 코드 (점령의 regionId) */
    @Query("select distinct c.regionId from TerritoryRegionCell c " +
            "where c.regionId is not null and c.parentName = :sidoName and c.regionName = :sigunguName")
    List<String> findRegionIdsByNames(@Param("sidoName") String sidoName, @Param("sigunguName") String sigunguName);
}
