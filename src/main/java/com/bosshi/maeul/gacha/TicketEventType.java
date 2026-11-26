package com.bosshi.maeul.gacha;

/** 가챠권을 주는 이벤트 종류. 이벤트마다 발급 여부와 유효기간을 관리자가 정한다 (ADR 0016) */
public enum TicketEventType {
    /** 일정 완료 때 서버 가챠에 더해 1장 */
    ITINERARY_COMPLETION
}
