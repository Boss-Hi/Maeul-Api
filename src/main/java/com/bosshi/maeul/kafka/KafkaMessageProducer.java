package com.bosshi.maeul.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaMessageProducer implements MessageProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendMessage(String topic, String message) {
        // 지정된 토픽으로 메시지 발행
        kafkaTemplate.send(topic, message);
    }
}