package com.bosshi.maeul.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        // Object를 JSON 형태로 직렬화하기 위해 JsonSerializer 사용
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        // 최신 버전 JsonSerializer 필수 설정: 패키지 신뢰 및 타입 헤더 비활성화(또는 필요시 활성화)
        configProps.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);

        // ObjectMapper bean 유무와 관계없이 Kafka 직렬화 전용 Mapper를 구성
        ObjectMapper kafkaObjectMapper = new ObjectMapper();
        kafkaObjectMapper.registerModule(new JavaTimeModule());
        kafkaObjectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // JsonSerializer에 커스텀 ObjectMapper 장착
        JsonSerializer<Object> jsonSerializer = new JsonSerializer<>(kafkaObjectMapper);

        return new DefaultKafkaProducerFactory<>(configProps, new StringSerializer(), jsonSerializer);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}