package com.bosshi.maeul.territory.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 펫 분양. region_subdivisions 의 region_code 와 code 를 각각 보낸다 (GET /api/fats/regions)
 *
 * @param regionCode 시도 코드 (예: 11, 세종특별자치시는 36110)
 * @param code       시군구 코드 (예: 560, 세종특별자치시는 36110)
 */
public record AdoptFatRequest(
        @NotBlank(message = "시도 코드(regionCode)는 필수입니다.")
        String regionCode,
        @NotBlank(message = "시군구 코드(code)는 필수입니다.")
        String code
) {
}
