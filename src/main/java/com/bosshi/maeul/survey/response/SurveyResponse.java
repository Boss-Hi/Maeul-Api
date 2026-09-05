package com.bosshi.maeul.survey.response;

import com.bosshi.maeul.survey.entity.Survey;
import com.bosshi.maeul.survey.entity.SurveyQuestion;
import com.bosshi.maeul.survey.entity.SurveyQuestionOption;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class SurveyResponse {
    private Long surveyId;
    private String code;
    private String title;
    private Integer totalSteps;
    private List<QuestionResponse> questions;

    public static SurveyResponse from(Survey survey) {
        List<QuestionResponse> questionResponses = survey.getQuestions().stream()
                .map(QuestionResponse::from)
                .toList();

        return SurveyResponse.builder()
                .surveyId(survey.getId())
                .code(survey.getCode())
                .title(survey.getTitle())
                .totalSteps(questionResponses.size()) // 전체 문항 수
                .questions(questionResponses)
                .build();
    }

    @Getter
    @Builder
    public static class QuestionResponse {
        private Long questionId;
        private Integer stepOrder;
        private String stepBadge;
        private String title;
        private String subTitle;
        private String questionType;
        private Boolean isRequired;
        private List<OptionResponse> options;

        public static QuestionResponse from(SurveyQuestion question) {
            return QuestionResponse.builder()
                    .questionId(question.getId())
                    .stepOrder(question.getStepOrder())
                    .stepBadge(question.getStepBadge())
                    .title(question.getTitle())
                    .subTitle(question.getSubTitle())
                    .questionType(question.getQuestionType().name())
                    .isRequired(question.getIsRequired())
                    .options(question.getOptions().stream().map(OptionResponse::from).toList())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class OptionResponse {
        private Long optionId;
        private String label;
        private String subLabel;
        private String value;
        private String iconUrl;

        public static OptionResponse from(SurveyQuestionOption option) {
            return OptionResponse.builder()
                    .optionId(option.getId())
                    .label(option.getLabel())
                    .subLabel(option.getSubLabel())
                    .value(option.getValue())
                    .iconUrl(option.getIconUrl())
                    .build();
        }
    }
}