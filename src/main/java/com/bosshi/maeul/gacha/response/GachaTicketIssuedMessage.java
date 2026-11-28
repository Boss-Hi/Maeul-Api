package com.bosshi.maeul.gacha.response;

import com.bosshi.maeul.gacha.GachaTicketIssuedEvent;
import com.bosshi.maeul.gacha.TicketEventType;

import java.time.LocalDateTime;

/** 가챠권 발급 알림 페이로드. QR 코드와 내부 refKey 는 담지 않는다 */
public record GachaTicketIssuedMessage(Long ticketId, TicketEventType eventType, LocalDateTime expiresAt) {

    public static GachaTicketIssuedMessage from(GachaTicketIssuedEvent event) {
        return new GachaTicketIssuedMessage(event.ticketId(), event.eventType(), event.expiresAt());
    }
}
