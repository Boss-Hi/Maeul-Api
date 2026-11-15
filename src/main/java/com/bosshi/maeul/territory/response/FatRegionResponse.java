package com.bosshi.maeul.territory.response;

import com.bosshi.maeul.territory.dto.FatRegion;

/**
 * 병아리를 키울 수 있는 시군구 (region_subdivisions)
 *
 * @param regionCode 시도 코드. 분양할 때 code 와 함께 보낸다
 * @param code       시군구 코드. 분양할 때 regionCode 와 함께 보낸다
 */
public record FatRegionResponse(Long regionSubdivisionId, String regionCode, String code, String sidoName, String sigunguName) {

    public static FatRegionResponse from(FatRegion region) {
        return new FatRegionResponse(region.regionSubdivisionId(), region.regionCode(), region.code(), region.sidoName(), region.sigunguName());
    }
}
