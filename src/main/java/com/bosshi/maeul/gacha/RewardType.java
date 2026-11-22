package com.bosshi.maeul.gacha;

/** 가챠 결과가 주는 보상 */
public enum RewardType {
    /** 상품. 교환권을 발급한다 */
    PRIZE,
    /** 아이템. 사용자 보유 수량이 는다 */
    ITEM,
    /** 포인트 */
    POINT,
    /** 펫 경험치. 가챠를 부른 쪽(펫)이 기록한다 */
    FAT_EXP,
    /** 꽝 */
    NONE
}
