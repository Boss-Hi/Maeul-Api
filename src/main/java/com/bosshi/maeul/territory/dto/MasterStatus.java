package com.bosshi.maeul.territory.dto;

import com.bosshi.maeul.territory.service.FatLevelTable;

/**
 * 주인의 경험치와 주인 레벨표로 계산한 진행 상태
 *
 * @param fatExpSum 주인 경험치의 바탕이 된 펫 경험치 합 (키우는 중은 지금 값, 졸업은 졸업 때 값)
 */
public record MasterStatus(long fatExpSum, FatLevelTable.Progress progress) {
}
