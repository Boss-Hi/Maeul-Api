package com.bosshi.maeul.survey.kafka;

import org.springframework.modulith.NamedInterface;

/**
 * 설문 답변 조회 요청/응답 토픽. 요청자는 reply 헤더로 응답 토픽을 지정한다.
 */
@NamedInterface
public class SurveyTopic {
    public static final String ANSWERS_REQUEST = "survey-answers-request";
    public static final String ANSWERS_REPLY = "survey-answers-reply";

    private SurveyTopic() {
    }
}
