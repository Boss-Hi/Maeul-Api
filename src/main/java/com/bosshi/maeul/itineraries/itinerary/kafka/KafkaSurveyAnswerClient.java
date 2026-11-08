package com.bosshi.maeul.itineraries.itinerary.kafka;

import com.bosshi.maeul.common.exception.ServiceUnavailableException;
import com.bosshi.maeul.itineraries.itinerary.client.SurveyAnswerClient;
import com.bosshi.maeul.itineraries.itinerary.dto.SurveyAnswerInfo;
import com.bosshi.maeul.survey.kafka.SurveyAnswersReply;
import com.bosshi.maeul.survey.kafka.SurveyAnswersRequest;
import com.bosshi.maeul.survey.kafka.SurveyTopic;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Kafka 요청/응답으로 survey 에서 사용자의 설문 답변을 가져온다. 응답이 올 때까지 기다린다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaSurveyAnswerClient implements SurveyAnswerClient {
    private static final String UNAVAILABLE_MESSAGE = "일정 생성 서비스를 일시적으로 사용할 수 없습니다. 잠시 후 다시 시도해주세요.";
    /** 새 소비자 그룹은 브로커의 초기 리밸런스 지연(약 3초) 뒤에 파티션을 받는다. 앱 시작 직후 요청만 기다린다 */
    private static final Duration ASSIGNMENT_WAIT = Duration.ofSeconds(5);

    private final ReplyingKafkaTemplate<String, Object, Object> template;

    @Override
    public List<SurveyAnswerInfo> findAnswers(Long userId) {
        try {
            // 응답 컨테이너가 파티션을 받기 전에 요청하면 응답을 놓친다
            if (!template.waitForAssignment(ASSIGNMENT_WAIT)) {
                throw new IllegalStateException("설문 응답 컨테이너가 준비되지 않았습니다.");
            }
            ConsumerRecord<String, Object> reply = template
                    .sendAndReceive(new ProducerRecord<>(SurveyTopic.ANSWERS_REQUEST, new SurveyAnswersRequest(userId)))
                    .get(SurveyAnswerReplyConfig.REPLY_TIMEOUT.toMillis() + 1_000, TimeUnit.MILLISECONDS);
            return ((SurveyAnswersReply) reply.value()).answers().stream()
                    .map(a -> new SurveyAnswerInfo(a.questionTitle(), a.optionLabel(), a.optionSubLabel(), a.optionValue()))
                    .toList();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw unavailable(userId, e);
        } catch (ExecutionException | TimeoutException | RuntimeException e) {
            throw unavailable(userId, e);
        }
    }

    private ServiceUnavailableException unavailable(Long userId, Exception cause) {
        log.error("설문 답변 조회 실패 - UserID: {}", userId, cause);
        return new ServiceUnavailableException(UNAVAILABLE_MESSAGE);
    }
}
