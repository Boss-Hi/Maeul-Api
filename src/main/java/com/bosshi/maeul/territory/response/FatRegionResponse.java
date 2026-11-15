package com.bosshi.maeul.territory.response;

import com.bosshi.maeul.territory.dto.FatRegion;

/**
 * 병아리를 키울 수 있는 시군구
 *
 * @param legalDongCodeId 분양할 때 보내는 시군구 ID
 * @param legalDongCode   시군구 법정동 코드 (점령 API 의 regionId 와 같은 값)
 * @param sigunguName     세종특별자치시처럼 시군구가 없으면 null
 */
public record FatRegionResponse(Long legalDongCodeId, String legalDongCode, String sidoName, String sigunguName) {

    public static FatRegionResponse from(FatRegion region) {
        return new FatRegionResponse(region.legalDongCodeId(), region.legalDongCode(), region.sidoName(), region.sigunguName());
    }
}
