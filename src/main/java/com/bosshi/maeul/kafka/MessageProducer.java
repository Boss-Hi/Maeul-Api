package com.bosshi.maeul.kafka;

public interface MessageProducer {
    /**
     * 지정된 토픽으로 메시지를 발행합니다.
     *
     * @param topic   발행할 토픽 이름
     * @param message 발행할 메시지 내용
     */
    void sendMessage(String topic, Object message);
}