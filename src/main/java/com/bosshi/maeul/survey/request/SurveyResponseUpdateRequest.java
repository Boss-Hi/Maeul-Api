package com.bosshi.maeul.survey.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class SurveyResponseUpdateRequest {
    private Long surveyResponseId;
    private List<AnswerUpdateRequest> answers;

    @Getter
    @NoArgsConstructor
    public static class AnswerUpdateRequest {
        private Long id;       // 수정할 SurveyAnswer 의 PK
        private Long optionId; // 변경할 Option 의 PK
    }
}
