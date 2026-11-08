package com.bosshi.maeul.survey.kafka;

import org.springframework.modulith.NamedInterface;

/**
 * 사용자의 현재 설문 답변 조회 요청
 */
@NamedInterface
public record SurveyAnswersRequest(Long userId) {
}
