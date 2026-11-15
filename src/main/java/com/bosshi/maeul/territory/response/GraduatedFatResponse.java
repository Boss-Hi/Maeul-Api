package com.bosshi.maeul.territory.response;

import com.bosshi.maeul.territory.dto.GraduatedFat;

import java.time.LocalDateTime;

/**
 * 졸업한 병아리. level, exp 는 졸업 때 값이다.
 */
public record GraduatedFatResponse(
        Long fatId,
        String regionId,
        String regionName,
        LocalDateTime adoptedAt,
        LocalDateTime graduatedAt,
        int level,
        long exp
) {
    public static GraduatedFatResponse from(GraduatedFat fat) {
        return new GraduatedFatResponse(fat.fatId(), fat.regionId(), fat.regionName(), fat.adoptedAt(),
                fat.graduatedAt(), fat.level(), fat.exp());
    }
}
