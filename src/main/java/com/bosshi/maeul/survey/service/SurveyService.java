package com.bosshi.maeul.survey.service;

import com.bosshi.maeul.survey.entity.*;
import com.bosshi.maeul.survey.repository.SurveyQuestionOptionRepository;
import com.bosshi.maeul.survey.repository.SurveyQuestionRepository;
import com.bosshi.maeul.survey.repository.SurveyRepository;
import com.bosshi.maeul.survey.repository.SurveyResponseRepository;
import com.bosshi.maeul.survey.request.SurveySubmitRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SurveyService {

    private final SurveyRepository surveyRepository;
    private final SurveyQuestionRepository questionRepository;
    private final SurveyQuestionOptionRepository optionRepository;
    private final SurveyResponseRepository responseRepository;

    public Survey getSurveyByCode(String code) {
        return surveyRepository.findByCodeAndActiveIsTrue(code)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 비활성화된 설문입니다. Code: " + code));
    }

    public Survey getFirst() {
        return surveyRepository.findAll().stream()
                .filter(Survey::getActive)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("활성화된 설문이 존재하지 않습니다."));
    }

    @Transactional
    public SurveyResponse submitSurvey(Long userId, SurveySubmitRequest request) {
        Survey survey = surveyRepository.findById(request.getSurveyId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 설문입니다. ID: " + request.getSurveyId()));

        SurveyResponse response = SurveyResponse.builder()
                .survey(survey)
                .userId(userId)
                .build();

        // 요청으로 온 답변 리스트 순회
        for (SurveySubmitRequest.AnswerRequest answerDto : request.getAnswers()) {
            SurveyQuestion question = questionRepository.findById(answerDto.getQuestionId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 질문입니다. ID: " + answerDto.getQuestionId()));

            // 선택된 옵션 목록 가져오기
            List<SurveyQuestionOption> options = optionRepository.findAllById(answerDto.getSelectedOptionIds());

            // 질문의 선택 옵션별로 SurveyAnswer 생성 및 연결
            for (SurveyQuestionOption option : options) {
                SurveyAnswer answer = SurveyAnswer.builder()
                        .question(question)
                        .option(option)
                        .build();

                response.addAnswer(answer);
            }
        }

        return responseRepository.save(response);
    }
}