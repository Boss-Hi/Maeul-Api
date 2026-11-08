package com.bosshi.maeul.itineraries.openapi.type;

/**
 * 축제 진행 상태 필터 (기준일: 오늘, Asia/Seoul)
 */
public enum FestivalStatus {
    /** 전체 (예정 포함) */
    ALL,
    /** 진행중: 시작일 <= 오늘 <= 종료일 */
    ONGOING,
    /** 종료: 종료일 < 오늘 */
    ENDED
}
