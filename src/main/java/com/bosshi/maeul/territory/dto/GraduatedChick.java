package com.bosshi.maeul.territory.dto;

import java.time.LocalDateTime;

/**
 * 졸업한 병아리. 레벨과 경험치는 졸업 때 값이다.
 */
public record GraduatedChick(
        Long chickId,
        String regionId,
        String regionName,
        LocalDateTime adoptedAt,
        LocalDateTime graduatedAt,
        int level,
        long exp
) {
}
