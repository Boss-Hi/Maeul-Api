package com.bosshi.maeul.territory.dto;

import com.bosshi.maeul.territory.entity.KakaoLegalDongCode;

/**
 * 병아리를 키우는 시군구 (카카오 법정동 코드 표의 한 행)
 *
 * @param legalDongCode 시군구 법정동 코드 (점령의 regionId 와 같은 값)
 * @param sigunguName   세종특별자치시처럼 시군구가 없으면 null
 */
public record FatRegion(Long legalDongCodeId, String legalDongCode, String sidoName, String sigunguName) {

    public static FatRegion from(KakaoLegalDongCode code) {
        return new FatRegion(code.getId(), code.getCode(), code.getSidoName(), code.getSigunguName());
    }
}
