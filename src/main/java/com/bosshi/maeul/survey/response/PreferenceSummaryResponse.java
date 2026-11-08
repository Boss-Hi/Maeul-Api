package com.bosshi.maeul.survey.response;

import com.bosshi.maeul.survey.dto.PreferenceSummary;

import java.util.List;

/**
 * 일정 설정 화면의 "사전 등록 정보" 카드. 설문에 답한 적이 없으면 ageGroup, occupation 은 null, 목록은 비어 있다.
 *
 * @param ageGroup   연령 (예: 20대)
 * @param occupation 직업 (예: 직장인)
 * @param lodgings   선호 숙소 유형
 * @param themes     선호 테마
 */
public record PreferenceSummaryResponse(Choice ageGroup, Choice occupation, List<Choice> lodgings, List<Choice> themes) {

    /**
     * @param label 화면에 보여줄 이름
     * @param value 옵션 값 (예: age_20)
     */
    public record Choice(String label, String value) {
        static Choice from(PreferenceSummary.Choice choice) {
            return choice == null ? null : new Choice(choice.label(), choice.value());
        }
    }

    public static PreferenceSummaryResponse from(PreferenceSummary summary) {
        return new PreferenceSummaryResponse(
                Choice.from(summary.ageGroup()),
                Choice.from(summary.occupation()),
                summary.lodgings().stream().map(Choice::from).toList(),
                summary.themes().stream().map(Choice::from).toList());
    }
}
