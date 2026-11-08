package com.bosshi.maeul.survey.kafka;

import org.springframework.modulith.NamedInterface;

import java.util.List;

/**
 * 사용자의 현재 설문 답변. 답변 1개 = 선택한 옵션 1개이며, 설문 질문 순서·옵션 순서대로 담긴다.
 * 응답이 없으면 빈 목록이다.
 */
@NamedInterface
public record SurveyAnswersReply(List<Answer> answers) {

    public record Answer(String questionTitle, String optionLabel, String optionSubLabel, String optionValue) {
    }
}
