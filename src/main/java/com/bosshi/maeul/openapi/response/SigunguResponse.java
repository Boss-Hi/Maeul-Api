package com.bosshi.maeul.openapi.response;

import com.bosshi.maeul.openapi.entity.LDongSigungu;

public record SigunguResponse(
        String code,
        String name
) {
    // LDongSigungu 엔티티에 code, name 필드가 있다고 가정
    public static SigunguResponse from(LDongSigungu sigungu) {
        return new SigunguResponse(
                sigungu.getCode(),
                sigungu.getName()
        );
    }
}
