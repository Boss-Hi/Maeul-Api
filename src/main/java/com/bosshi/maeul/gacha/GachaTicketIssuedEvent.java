package com.bosshi.maeul.gacha;

import java.time.LocalDateTime;

/** 가챠권을 새로 저장했을 때 보낸다. 커밋 뒤에 접속 중인 사용자에게 알린다 */
public record GachaTicketIssuedEvent(Long userId, Long ticketId, TicketEventType eventType, LocalDateTime expiresAt) {
}
