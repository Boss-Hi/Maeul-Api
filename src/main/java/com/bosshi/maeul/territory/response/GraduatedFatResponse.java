package com.bosshi.maeul.territory.response;

import com.bosshi.maeul.territory.dto.GraduatedFat;

import java.time.LocalDateTime;

/**
 * 졸업한 펫. level, exp 는 졸업 때 값이다.
 */
public record GraduatedFatResponse(
        Long fatId,
        Long regionSubdivisionId,
        String regionCode,
        String code,
        String sidoName,
        String sigunguName,
        LocalDateTime adoptedAt,
        LocalDateTime graduatedAt,
        int level,
        long exp
) {
    public static GraduatedFatResponse from(GraduatedFat fat) {
        return new GraduatedFatResponse(fat.fatId(), fat.region().regionSubdivisionId(), fat.region().regionCode(), fat.region().code(),
                fat.region().sidoName(), fat.region().sigunguName(), fat.adoptedAt(), fat.graduatedAt(), fat.level(), fat.exp());
    }
}
