package com.bosshi.maeul.territory.response;

import com.bosshi.maeul.territory.dto.TerritoryRegion;
import com.bosshi.maeul.territory.type.TerritoryRegionType;

/**
 * 지역 (전국 KR / 시도 2자리 / 시군구 5자리 코드를 같은 체계로 쓴다)
 */
public record TerritoryRegionResponse(
        String regionId,
        String name,
        String parentRegionId,
        TerritoryRegionType regionType
) {
    public static TerritoryRegionResponse from(TerritoryRegion region) {
        return new TerritoryRegionResponse(region.regionId(), region.name(), region.parentRegionId(), region.regionType());
    }
}
