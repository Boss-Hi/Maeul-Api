package com.bosshi.maeul.survey.response;

import com.bosshi.maeul.survey.entity.SurveyAnswer;
import com.bosshi.maeul.survey.entity.SurveyQuestion;
import com.bosshi.maeul.survey.entity.SurveyResponse;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class SurveyResResponse {
    private Long id;
    private List<SurveyAnswerResponse> answers;

    public static SurveyResResponse from(SurveyResponse surveyResponse) {
        List<SurveyAnswerResponse> answerResponses = surveyResponse.getAnswers().stream()
                .map(SurveyAnswerResponse::from)
                .toList();

        return SurveyResResponse.builder()
                .id(surveyResponse.getId())
                .answers(answerResponses)
                .build();
    }

    @Getter
    @Builder
    public static class SurveyAnswerResponse {
        private Long questionId;
        private Long optionId;

        public static SurveyAnswerResponse from(SurveyAnswer surveyAnswer) {
            SurveyQuestion question = surveyAnswer.getQuestion();
            return SurveyAnswerResponse.builder()
                    .questionId(question.getId())
                    .optionId(surveyAnswer.getOption() != null ? surveyAnswer.getOption().getId() : null)
                    .build();
        }
    }
}