package com.bosshi.maeul.gacha;

/**
 * 오프라인 가챠권을 발급하는 계약 (ADR 0016). 다른 모듈은 이벤트가 일어난 순간 이것을 부른다.
 */
public interface GachaTicketIssuer {

    /**
     * 같은 사용자, 이벤트, refKey 로 이미 발급했거나 그 이벤트의 발급이 꺼져 있으면 아무것도 하지 않는다.
     *
     * @param refKey 발급을 일으킨 일 (예: itinerary:12)
     */
    void issue(Long userId, TicketEventType eventType, String refKey);
}
