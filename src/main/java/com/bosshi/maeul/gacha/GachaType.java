package com.bosshi.maeul.gacha;

/** 가챠 종류. 가챠마다 결과 목록이 따로 있다 */
public enum GachaType {
    /** 펫 졸업 때 1회: 상품, 아이템, 포인트 */
    GRADUATION,
    /** 일정 완료 때 1회: 아이템, 포인트, 꽝 */
    ITINERARY_COMPLETION,
    /** 미션 완료 때 (그 시군구에 키우는 펫이 있을 때만): 펫 경험치 */
    MISSION
}
