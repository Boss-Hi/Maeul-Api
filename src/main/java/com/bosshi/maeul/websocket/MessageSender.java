package com.bosshi.maeul.websocket;

public interface MessageSender {

    /**
     * 특정 사용자에게 실시간 메시지 전송
     */
    void sendToUser(Long userId, String destination, Object payload);

    /**
     * 모든 구독자에게 브로드캐스트 메시지 전송
     */
    void convertAndSend(String destination, Object payload);
}