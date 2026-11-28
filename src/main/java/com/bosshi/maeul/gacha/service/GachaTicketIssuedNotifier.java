package com.bosshi.maeul.gacha.service;

import com.bosshi.maeul.gacha.GachaTicketIssuedEvent;
import com.bosshi.maeul.gacha.response.GachaTicketIssuedMessage;
import com.bosshi.maeul.websocket.MessageSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/** 가챠권 발급이 커밋된 뒤 접속 중인 그 사용자에게만 알린다. 놓친 알림은 다시 보내지 않는다 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GachaTicketIssuedNotifier {

    /** 구독 경로: /private/queue/gacha-ticket-issued */
    public static final String DESTINATION = "gacha-ticket-issued";

    private final MessageSender messageSender;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTicketIssued(GachaTicketIssuedEvent event) {
        try {
            messageSender.sendToUser(event.userId(), DESTINATION, GachaTicketIssuedMessage.from(event));
        } catch (RuntimeException e) {
            // 알림은 부가 기능이라 발급과 호출자의 응답을 깨뜨리지 않는다
            log.error("[가챠권 발급 알림 실패] userId={}, ticketId={}", event.userId(), event.ticketId(), e);
        }
    }
}
