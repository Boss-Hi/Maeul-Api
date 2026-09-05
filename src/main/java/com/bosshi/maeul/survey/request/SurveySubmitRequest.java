package com.bosshi.maeul.survey.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.List;

@Getter
public class SurveySubmitRequest {
    @NotNull(message = "surveyId는 필수입니다.")
    private Long surveyId;

    @NotEmpty(message = "답변 항목은 최소 1개 이상이어야 합니다.")
    private List<AnswerRequest> answers;

    @Getter
    public static class AnswerRequest {
        @NotNull(message = "questionId는 필수입니다.")
        private Long questionId;

        @NotEmpty(message = "선택된 옵션 ID는 최소 1개 이상이어야 합니다.")
        private List<Long> selectedOptionIds;
    }
}
