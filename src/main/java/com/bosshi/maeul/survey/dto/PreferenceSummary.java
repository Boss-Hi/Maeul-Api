package com.bosshi.maeul.survey.dto;

import java.util.List;

/**
 * 사용자가 설문에 입력한 값 중 일정 설정 화면의 "사전 등록 정보" 카드에 보여줄 것.
 * 설문에 답한 적이 없거나 해당 질문에 답하지 않았으면 ageGroup, occupation 은 null, 목록은 비어 있다.
 *
 * @param lodgings 선호하는 숙소 유형 (옵션 순서대로)
 * @param themes   끌리는 테마 (옵션 순서대로)
 */
public record PreferenceSummary(Choice ageGroup, Choice occupation, List<Choice> lodgings, List<Choice> themes) {

    /**
     * 선택한 옵션 1개
     */
    public record Choice(String label, String value) {
    }
}
