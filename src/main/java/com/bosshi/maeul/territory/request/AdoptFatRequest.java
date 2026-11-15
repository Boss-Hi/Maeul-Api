package com.bosshi.maeul.territory.request;

import jakarta.validation.constraints.NotNull;

/**
 * 병아리 분양
 *
 * @param legalDongCodeId 시군구 ID (GET /api/fats/regions 의 legalDongCodeId)
 */
public record AdoptFatRequest(
        @NotNull(message = "시군구 ID 는 필수입니다.")
        Long legalDongCodeId
) {
}
