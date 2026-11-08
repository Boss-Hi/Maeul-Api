package com.bosshi.maeul.itinerary.kafka;

import com.bosshi.maeul.survey.kafka.SurveyTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

/**
 * 설문 답변 요청/응답용 템플릿. 응답 컨테이너는 인스턴스마다 고유한 그룹으로 응답 토픽만 읽는다.
 */
@Configuration
public class SurveyAnswerReplyConfig {
    static final Duration REPLY_TIMEOUT = Duration.ofSeconds(5);
    /** 브로커에 닿지 못할 때 요청이 오래 막히지 않도록 */
    private static final int SEND_BLOCK_MS = 2_000;

    @Bean
    public ReplyingKafkaTemplate<String, Object, Object> surveyAnswersReplyingTemplate(
            ProducerFactory<String, Object> producerFactory,
            ConcurrentKafkaListenerContainerFactory<String, Object> listenerContainerFactory) {
        ConcurrentMessageListenerContainer<String, Object> replyContainer =
                listenerContainerFactory.createContainer(SurveyTopic.ANSWERS_REPLY);
        replyContainer.getContainerProperties().setGroupId("itinerary-survey-reply-" + UUID.randomUUID());
        // 새 그룹이 이전 응답을 처음부터 다시 읽지 않도록 한다
        replyContainer.getContainerProperties().setKafkaConsumerProperties(
                toProperties(Map.of(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest")));

        ReplyingKafkaTemplate<String, Object, Object> template = new ReplyingKafkaTemplate<>(
                producerFactory.copyWithConfigurationOverride(Map.of(ProducerConfig.MAX_BLOCK_MS_CONFIG, SEND_BLOCK_MS)),
                replyContainer);
        template.setDefaultReplyTimeout(REPLY_TIMEOUT);
        return template;
    }

    private static java.util.Properties toProperties(Map<String, Object> values) {
        java.util.Properties properties = new java.util.Properties();
        properties.putAll(values);
        return properties;
    }
}
