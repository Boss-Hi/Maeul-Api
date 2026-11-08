package com.bosshi.maeul.itineraries.itinerary.dto;

/**
 * 일정 생성에 반영할 사용자의 설문 답변 1개 (선택한 옵션 1개)
 */
public record SurveyAnswerInfo(String questionTitle, String optionLabel, String optionSubLabel, String optionValue) {
}
