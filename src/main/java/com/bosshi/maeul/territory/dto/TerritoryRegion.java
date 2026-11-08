package com.bosshi.maeul.territory.dto;

import com.bosshi.maeul.territory.type.TerritoryRegionType;

/**
 * 지역 (전국 KR / 시도 2자리 / 시군구 5자리 코드를 같은 체계로 쓴다)
 */
public record TerritoryRegion(
        String regionId,
        String name,
        String parentRegionId,
        TerritoryRegionType regionType
) {
}
