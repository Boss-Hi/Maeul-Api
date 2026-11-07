package com.bosshi.maeul.survey.service;

import com.bosshi.maeul.survey.entity.Survey;
import com.bosshi.maeul.survey.entity.SurveyAnswer;
import com.bosshi.maeul.survey.entity.SurveyQuestion;
import com.bosshi.maeul.survey.entity.SurveyQuestionOption;
import com.bosshi.maeul.survey.entity.SurveyResponse;
import com.bosshi.maeul.survey.request.SurveySubmitRequest;
import com.bosshi.maeul.survey.type.QuestionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("SurveyAnswerFactory 테스트")
class SurveyAnswerFactoryTest {

    private final SurveyAnswerFactory factory = new SurveyAnswerFactory();

    private Survey survey;

    // Q1: 단일 선택(필수) - 옵션 11, 12
    // Q2: 복수 선택(필수) - 옵션 21, 22, 23
    // Q3: 단일 선택(선택) - 옵션 31
    @BeforeEach
    void setUp() {
        survey = Survey.builder().id(100L).code("S").title("설문").build();
        survey.getQuestions().add(question(1L, QuestionType.SINGLE_CHOICE, true, 11L, 12L));
        survey.getQuestions().add(question(2L, QuestionType.MULTIPLE_CHOICE, true, 21L, 22L, 23L));
        survey.getQuestions().add(question(3L, QuestionType.SINGLE_CHOICE, false, 31L));
    }

    @Test
    @DisplayName("선택한 옵션 1개당 답변 1개를 만든다")
    void createsOneAnswerPerSelectedOption() {
        List<SurveyAnswer> answers = factory.create(survey, List.of(
                answer(1L, 11L),
                answer(2L, 21L, 23L)
        ));

        assertThat(answers)
                .extracting(a -> a.getQuestion().getId(), a -> a.getOption().getId())
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(1L, 11L),
                        org.assertj.core.groups.Tuple.tuple(2L, 21L),
                        org.assertj.core.groups.Tuple.tuple(2L, 23L)
                );
    }

    @Test
    @DisplayName("단일 선택 질문에 옵션을 2개 고르면 거절한다")
    void rejectsMultipleOptionsForSingleChoice() {
        assertThatThrownBy(() -> factory.create(survey, List.of(answer(1L, 11L, 12L), answer(2L, 21L))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("단일 선택");
    }

    @Test
    @DisplayName("다른 질문의 옵션을 고르면 거절한다")
    void rejectsOptionOfAnotherQuestion() {
        assertThatThrownBy(() -> factory.create(survey, List.of(answer(1L, 21L), answer(2L, 22L))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("질문에 속하지 않는 옵션");
    }

    @Test
    @DisplayName("필수 질문이 빠지면 거절한다")
    void rejectsMissingRequiredQuestion() {
        assertThatThrownBy(() -> factory.create(survey, List.of(answer(1L, 11L))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("필수 질문");
    }

    @Test
    @DisplayName("설문에 없는 질문이면 거절한다")
    void rejectsUnknownQuestion() {
        assertThatThrownBy(() -> factory.create(survey, List.of(answer(1L, 11L), answer(2L, 21L), answer(999L, 11L))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("존재하지 않는 질문");
    }

    @Test
    @DisplayName("같은 질문이나 같은 옵션이 중복되면 거절한다")
    void rejectsDuplicates() {
        assertThatThrownBy(() -> factory.create(survey, List.of(answer(1L, 11L), answer(1L, 12L), answer(2L, 21L))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("중복");
        assertThatThrownBy(() -> factory.create(survey, List.of(answer(1L, 11L), answer(2L, 21L, 21L))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("중복");
    }

    @Test
    @DisplayName("응답의 답변을 교체하면 기존 답변은 사라지고 새 답변만 남는다 (2개 → 1개)")
    void replaceAnswersShrinksSelection() {
        SurveyResponse response = SurveyResponse.builder().id(1L).survey(survey).userId(7L).build();
        factory.create(survey, List.of(answer(1L, 11L), answer(2L, 21L, 22L))).forEach(response::addAnswer);
        assertThat(response.getAnswers()).hasSize(3);

        response.replaceAnswers(factory.create(survey, List.of(answer(1L, 12L), answer(2L, 23L))));

        assertThat(response.getAnswers())
                .extracting(a -> a.getOption().getId())
                .containsExactly(12L, 23L);
        assertThat(response.getAnswers()).allMatch(a -> a.getSurveyResponse() == response);
    }

    private SurveyQuestion question(Long id, QuestionType type, boolean required, Long... optionIds) {
        SurveyQuestion question = SurveyQuestion.builder()
                .id(id).survey(survey).stepOrder(id.intValue()).title("Q" + id)
                .questionType(type).isRequired(required)
                .build();
        for (int i = 0; i < optionIds.length; i++) {
            question.getOptions().add(SurveyQuestionOption.builder()
                    .id(optionIds[i]).question(question).optionOrder(i).label("O" + optionIds[i]).value("v" + optionIds[i])
                    .build());
        }
        return question;
    }

    private SurveySubmitRequest.AnswerRequest answer(Long questionId, Long... optionIds) {
        SurveySubmitRequest.AnswerRequest request = new SurveySubmitRequest.AnswerRequest();
        ReflectionTestUtils.setField(request, "questionId", questionId);
        ReflectionTestUtils.setField(request, "selectedOptionIds", List.of(optionIds));
        return request;
    }
}
