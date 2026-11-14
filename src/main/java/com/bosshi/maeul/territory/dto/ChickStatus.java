package com.bosshi.maeul.territory.dto;

import com.bosshi.maeul.territory.service.ChickLevelTable;

import java.time.LocalDateTime;

/**
 * 키우는 중인 병아리와 지금 레벨표로 계산한 진행 상태
 */
public record ChickStatus(
        Long chickId,
        String regionId,
        String regionName,
        LocalDateTime adoptedAt,
        ChickLevelTable.Progress progress
) {
}
