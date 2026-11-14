package com.bosshi.maeul.territory.response;

import com.bosshi.maeul.territory.dto.GraduatedChick;

import java.time.LocalDateTime;

/**
 * 졸업한 병아리. level, exp 는 졸업 때 값이다.
 */
public record GraduatedChickResponse(
        Long chickId,
        String regionId,
        String regionName,
        LocalDateTime adoptedAt,
        LocalDateTime graduatedAt,
        int level,
        long exp
) {
    public static GraduatedChickResponse from(GraduatedChick chick) {
        return new GraduatedChickResponse(chick.chickId(), chick.regionId(), chick.regionName(), chick.adoptedAt(),
                chick.graduatedAt(), chick.level(), chick.exp());
    }
}
