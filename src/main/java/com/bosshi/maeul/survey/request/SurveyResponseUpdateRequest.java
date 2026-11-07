package com.bosshi.maeul.survey.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 현재 응답의 답변 전체를 교체하는 요청. 질문 단위 항목은 등록 요청과 동일하다.
 */
@Getter
@NoArgsConstructor
public class SurveyResponseUpdateRequest {
    @Valid
    @NotEmpty(message = "답변 항목은 최소 1개 이상이어야 합니다.")
    private List<SurveySubmitRequest.AnswerRequest> answers;
}
