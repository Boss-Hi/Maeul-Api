package com.bosshi.maeul.gacha;

/**
 * 가챠를 뽑는 계약 (ADR 0014). 다른 모듈은 일이 일어난 순간 이것을 불러 결과를 받는다.
 * 상품, 아이템, 포인트는 가챠 모듈이 지급하고, 펫 경험치는 부른 쪽이 결과의 amount 로 기록한다.
 */
public interface GachaDrawer {

    /**
     * 같은 사용자, 가챠, refKey 로 이미 뽑았으면 다시 뽑지 않고 그 결과를 돌려준다.
     *
     * @param refKey 가챠를 일으킨 일 (예: fat:3, mission:12)
     */
    GachaDraw draw(Long userId, GachaType gachaType, String refKey);
}
