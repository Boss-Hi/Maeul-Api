package com.bosshi.maeul.survey.service;

import com.bosshi.maeul.survey.dto.PreferenceSummary;
import com.bosshi.maeul.survey.dto.PreferenceSummary.Choice;
import com.bosshi.maeul.survey.entity.Survey;
import com.bosshi.maeul.survey.entity.SurveyAnswer;
import com.bosshi.maeul.survey.entity.SurveyQuestion;
import com.bosshi.maeul.survey.entity.SurveyQuestionOption;
import com.bosshi.maeul.survey.entity.SurveyResponse;
import com.bosshi.maeul.survey.repository.SurveyRepository;
import com.bosshi.maeul.survey.repository.SurveyResponseRepository;
import com.bosshi.maeul.survey.type.StepBadge;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("SurveyResponseService 사전 등록 정보 테스트")
class SurveyResponseServiceTest {

    private final SurveyRepository surveyRepository = mock(SurveyRepository.class);
    private final SurveyResponseRepository responseRepository = mock(SurveyResponseRepository.class);
    private final SurveyResponseService service = new SurveyResponseService(surveyRepository, responseRepository, mock(SurveyAnswerFactory.class));
    private final Survey survey = Survey.builder().id(1L).code("S").title("설문").build();

    @BeforeEach
    void setUp() {
        when(surveyRepository.findAll()).thenReturn(List.of(survey));
    }

    @Test
    @DisplayName("질문 배지로 나이대, 직업, 숙소, 테마를 골라 담고 복수 선택은 옵션 순서대로 담는다")
    void picksAnswersByBadge() {
        SurveyResponse response = SurveyResponse.builder().survey(survey).userId(7L).build();
        response.addAnswer(answer(StepBadge.THEME, 3, "음악/공연", "music_performance"));
        response.addAnswer(answer(StepBadge.AGE, 2, "20대", "age_20"));
        response.addAnswer(answer(StepBadge.THEME, 1, "커피", "coffee"));
        response.addAnswer(answer(StepBadge.STAY, 2, "펜션/민박", "pension_guesthouse"));
        response.addAnswer(answer(StepBadge.OCCUPATION, 1, "직장인", "worker"));
        response.addAnswer(answer(StepBadge.TRIP_PURPOSE, 1, "쉼과 회복", "rest_recovery"));
        when(responseRepository.findFirstByUserIdAndSurveyIdOrderByCreatedAtDesc(7L, 1L)).thenReturn(Optional.of(response));

        PreferenceSummary summary = service.currentPreferenceSummary(7L);

        assertThat(summary.ageGroup()).isEqualTo(new Choice("20대", "age_20"));
        assertThat(summary.occupation()).isEqualTo(new Choice("직장인", "worker"));
        assertThat(summary.lodgings()).containsExactly(new Choice("펜션/민박", "pension_guesthouse"));
        assertThat(summary.themes()).containsExactly(new Choice("커피", "coffee"), new Choice("음악/공연", "music_performance"));
    }

    @Test
    @DisplayName("응답한 적이 없으면 빈 값을 돌려준다")
    void emptyWhenNeverAnswered() {
        when(responseRepository.findFirstByUserIdAndSurveyIdOrderByCreatedAtDesc(7L, 1L)).thenReturn(Optional.empty());

        PreferenceSummary summary = service.currentPreferenceSummary(7L);

        assertThat(summary.ageGroup()).isNull();
        assertThat(summary.occupation()).isNull();
        assertThat(summary.lodgings()).isEmpty();
        assertThat(summary.themes()).isEmpty();
    }

    private static SurveyAnswer answer(StepBadge badge, int optionOrder, String label, String value) {
        SurveyQuestion question = SurveyQuestion.builder().stepOrder(1).stepBadge(badge.name()).title(badge.name()).build();
        SurveyQuestionOption option = SurveyQuestionOption.builder().optionOrder(optionOrder).label(label).value(value).build();
        return SurveyAnswer.builder().question(question).option(option).build();
    }
}
