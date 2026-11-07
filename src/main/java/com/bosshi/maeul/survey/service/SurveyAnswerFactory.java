package com.bosshi.maeul.survey.service;

import com.bosshi.maeul.survey.entity.Survey;
import com.bosshi.maeul.survey.entity.SurveyAnswer;
import com.bosshi.maeul.survey.entity.SurveyQuestion;
import com.bosshi.maeul.survey.entity.SurveyQuestionOption;
import com.bosshi.maeul.survey.request.SurveySubmitRequest;
import com.bosshi.maeul.survey.type.QuestionType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 설문 요청을 검증하고 답변(SurveyAnswer) 목록으로 변환한다.
 * 답변 1개 = 선택한 옵션 1개. 등록과 수정이 같은 규칙을 사용한다.
 */
@Component
public class SurveyAnswerFactory {

    public List<SurveyAnswer> create(Survey survey, List<SurveySubmitRequest.AnswerRequest> requests) {
        Map<Long, SurveyQuestion> questionMap = survey.getQuestions().stream()
                .collect(Collectors.toMap(SurveyQuestion::getId, Function.identity()));

        Set<Long> answeredQuestionIds = new HashSet<>();
        List<SurveyAnswer> answers = new ArrayList<>();

        for (SurveySubmitRequest.AnswerRequest request : requests) {
            SurveyQuestion question = questionMap.get(request.getQuestionId());
            if (question == null) {
                throw new IllegalArgumentException("설문에 존재하지 않는 질문입니다. ID: " + request.getQuestionId());
            }
            if (!answeredQuestionIds.add(question.getId())) {
                throw new IllegalArgumentException("같은 질문에 대한 답변이 중복되었습니다. ID: " + question.getId());
            }

            List<SurveyQuestionOption> options = resolveOptions(question, request.getSelectedOptionIds());
            for (SurveyQuestionOption option : options) {
                answers.add(SurveyAnswer.builder()
                        .question(question)
                        .option(option)
                        .build());
            }
        }

        for (SurveyQuestion question : survey.getQuestions()) {
            if (Boolean.TRUE.equals(question.getIsRequired()) && !answeredQuestionIds.contains(question.getId())) {
                throw new IllegalArgumentException("필수 질문에 대한 답변이 없습니다. ID: " + question.getId());
            }
        }

        return answers;
    }

    private List<SurveyQuestionOption> resolveOptions(SurveyQuestion question, List<Long> selectedOptionIds) {
        if (selectedOptionIds == null || selectedOptionIds.isEmpty()) {
            throw new IllegalArgumentException("선택된 옵션이 없습니다. 질문 ID: " + question.getId());
        }
        if (new HashSet<>(selectedOptionIds).size() != selectedOptionIds.size()) {
            throw new IllegalArgumentException("같은 옵션이 중복 선택되었습니다. 질문 ID: " + question.getId());
        }
        if (question.getQuestionType() == QuestionType.SINGLE_CHOICE && selectedOptionIds.size() != 1) {
            throw new IllegalArgumentException("단일 선택 질문은 옵션을 1개만 선택할 수 있습니다. 질문 ID: " + question.getId());
        }

        Map<Long, SurveyQuestionOption> optionMap = question.getOptions().stream()
                .collect(Collectors.toMap(SurveyQuestionOption::getId, Function.identity()));

        return selectedOptionIds.stream()
                .map(optionId -> {
                    SurveyQuestionOption option = optionMap.get(optionId);
                    if (option == null) {
                        throw new IllegalArgumentException("질문에 속하지 않는 옵션입니다. 질문 ID: " + question.getId() + ", 옵션 ID: " + optionId);
                    }
                    return option;
                })
                .toList();
    }
}
