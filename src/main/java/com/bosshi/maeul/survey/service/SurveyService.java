package com.bosshi.maeul.survey.service;

import com.bosshi.maeul.survey.entity.*;
import com.bosshi.maeul.survey.repository.SurveyRepository;
import com.bosshi.maeul.survey.repository.SurveyResponseRepository;
import com.bosshi.maeul.survey.request.SurveySubmitRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SurveyService {

    private final SurveyRepository surveyRepository;
    private final SurveyResponseRepository responseRepository;
    private final SurveyAnswerFactory answerFactory;

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

        answerFactory.create(survey, request.getAnswers()).forEach(response::addAnswer);

        return responseRepository.save(response);
    }
}