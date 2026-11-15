package com.bosshi.maeul.itineraries.openapi.repository;

import com.bosshi.maeul.itineraries.openapi.entity.RegionSubdivisions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.modulith.NamedInterface;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@NamedInterface
public interface LDongSigunguRepository extends JpaRepository<RegionSubdivisions, String>, JpaSpecificationExecutor<RegionSubdivisions> {

    /** 시도 코드(region_code)와 시군구 코드(code)를 각각 받아 시군구 1개를 찾는다 */
    @Query("select s from RegionSubdivisions s join fetch s.region r where r.code = :regionCode and s.code = :code")
    Optional<RegionSubdivisions> findWithRegionByRegionCodeAndCode(@Param("regionCode") String regionCode, @Param("code") String code);

    /** 아래에 구가 있는 시인지 (예: 수원시 아래 '수원시 장안구') */
    @Query("select count(s) > 0 from RegionSubdivisions s where s.region.code = :regionCode and s.name like concat(:name, ' %')")
    boolean hasDistrictsUnder(@Param("regionCode") String regionCode, @Param("name") String name);

    @Query("select s from RegionSubdivisions s join fetch s.region where s.id in :ids")
    List<RegionSubdivisions> findWithRegionByIdIn(@Param("ids") Collection<Long> ids);

    @Query("select s from RegionSubdivisions s join fetch s.region")
    List<RegionSubdivisions> findAllWithRegion();
}
