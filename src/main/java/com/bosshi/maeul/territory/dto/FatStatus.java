package com.bosshi.maeul.territory.dto;

import com.bosshi.maeul.territory.service.FatLevelTable;

import java.time.LocalDateTime;

/**
 * 키우는 중인 펫과 지금 레벨표로 계산한 진행 상태
 */
public record FatStatus(
        Long fatId,
        FatRegion region,
        LocalDateTime adoptedAt,
        FatLevelTable.Progress progress
) {
}
