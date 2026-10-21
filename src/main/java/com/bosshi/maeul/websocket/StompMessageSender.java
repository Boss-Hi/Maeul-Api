package com.bosshi.maeul.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Component
public class StompMessageSender implements MessageSender {

    private final SimpMessagingTemplate messagingTemplate;

    public StompMessageSender(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * 특정 사용자에게 메시지를 전송할 때 공통 로직(로깅, 타임스탬프, 커스텀 헤더 등)을 추가
     */
    public void sendToUser(Long userId, String destination, Object payload) {
        try {
            log.info("[WebSocket 전송] 대상 사용자: {}, 경로: {}, 페이로드: {}", userId, destination, payload);

            // 필요하다면 공통 헤더 추가 가능
            Map<String, Object> headers = Map.of(
                    "timestamp", LocalDateTime.now().toString(),
                    "server-id", "maeul-server-01"
            );

            messagingTemplate.convertAndSendToUser(userId.toString(), destination, payload, headers);

        } catch (Exception e) {
            log.error("[WebSocket 전송 실패] 대상 사용자: {}, 오류: {}", userId, e.getMessage(), e);
            throw new MessagingException("실시간 메시지 전송에 실패했습니다.", e);
        }
    }

    /**
     * 일반 브로드캐스트(모두에게 전송)용 커스텀 메서드
     */
    public void convertAndSend(String destination, Object payload) {
        log.info("[WebSocket 브로드캐스트] 경로: {}, 페이로드: {}", destination, payload);
        messagingTemplate.convertAndSend(destination, payload);
    }
}