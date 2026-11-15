package com.bosshi.maeul.territory.dto;

import com.bosshi.maeul.itineraries.openapi.entity.RegionSubdivisions;

/**
 * 펫을 키우는 시군구 (region_subdivisions 의 한 행)
 *
 * @param regionCode 시도 코드 (region_subdivisions.region_code)
 * @param code       시군구 코드 (region_subdivisions.code)
 */
public record FatRegion(Long regionSubdivisionId, String regionCode, String code, String sidoName, String sigunguName) {

    public static FatRegion from(RegionSubdivisions subdivision) {
        return new FatRegion(subdivision.getId(), subdivision.getRegion().getCode(), subdivision.getCode(),
                subdivision.getRegion().getName(), subdivision.getName());
    }
}
