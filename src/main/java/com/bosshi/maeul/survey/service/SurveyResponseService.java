package com.bosshi.maeul.survey.service;

import com.bosshi.maeul.survey.entity.SurveyResponse;
import com.bosshi.maeul.survey.repository.SurveyResponseRepository;
import com.bosshi.maeul.survey.request.SurveySubmitRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SurveyResponseService {

    private final SurveyResponseRepository responseRepository;

    public SurveyResponse getCurrentResponsesByUserId(Long userId, Long surveyId) {
        return responseRepository.findByUserIdAndSurveyId(userId, surveyId);
    }

    @Transactional
    public SurveyResponse updateBySurveyResponseId(Long surveyResponseId, SurveySubmitRequest request) {
        return responseRepository.findById(surveyResponseId)
                .map(surveyResponse -> {
                    // surveyResponse.updateAnswers(request);
                    return responseRepository.save(surveyResponse);
                })
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 설문 응답입니다. ID: " + surveyResponseId));
    }
}