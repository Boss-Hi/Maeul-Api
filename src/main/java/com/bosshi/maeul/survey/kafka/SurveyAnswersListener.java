package com.bosshi.maeul.survey.kafka;

import com.bosshi.maeul.survey.entity.SurveyAnswer;
import com.bosshi.maeul.survey.kafka.SurveyAnswersReply.Answer;
import com.bosshi.maeul.survey.service.SurveyResponseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;

@Slf4j
@Component
@RequiredArgsConstructor
public class SurveyAnswersListener {
    private static final String GROUP_ID = "survey-group";

    private final SurveyResponseService surveyResponseService;

    /**
     * 요청 헤더의 reply 토픽으로 사용자의 현재 설문 답변을 돌려준다.
     */
    @KafkaListener(topics = SurveyTopic.ANSWERS_REQUEST, groupId = GROUP_ID)
    @SendTo
    @Transactional(readOnly = true)
    public SurveyAnswersReply reply(SurveyAnswersRequest request) {
        log.info("Kafka 설문 답변 조회 요청 수신 - UserID: {}", request.userId());
        return new SurveyAnswersReply(surveyResponseService.findCurrentAnswers(request.userId()).stream()
                .sorted(Comparator
                        .comparing((SurveyAnswer a) -> a.getQuestion().getStepOrder())
                        .thenComparing(a -> a.getOption().getOptionOrder()))
                .map(a -> new Answer(
                        a.getQuestion().getTitle(),
                        a.getOption().getLabel(),
                        a.getOption().getSubLabel(),
                        a.getOption().getValue()))
                .toList());
    }
}
