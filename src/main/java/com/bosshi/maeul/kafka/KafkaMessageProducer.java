package com.bosshi.maeul.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaMessageProducer implements MessageProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendMessage(String topic, Object message) {
        log.info("Kafka 이벤트 발행 [토픽: {}] - 메시지: {}", topic, message);
        // 지정된 토픽으로 메시지 발행
        kafkaTemplate.send(topic, message);
    }
}