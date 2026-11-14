package com.bosshi.maeul.territory.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * 병아리 분양
 *
 * @param regionId 시군구 코드 5자리
 */
public record AdoptChickRequest(
        @NotNull(message = "시군구 코드는 필수입니다.")
        @Pattern(regexp = "\\d{5}", message = "시군구 코드는 5자리 숫자여야 합니다.")
        String regionId
) {
}
