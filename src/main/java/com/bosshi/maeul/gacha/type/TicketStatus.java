package com.bosshi.maeul.gacha.type;

/** 가챠권 상태. 사용됨 이후에는 만료되지 않는다 */
public enum TicketStatus {
    /** 사용 가능 */
    USABLE,
    /** 사장님이 스캔했고 상품 QR 은 아직 연결되지 않음 */
    USED,
    /** 상품 QR 이 연결됨 */
    PRIZE_RECEIVED,
    /** 쓰지 않고 유효기간이 지남 */
    EXPIRED
}
