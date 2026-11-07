package com.bosshi.maeul.survey.service;

import com.bosshi.maeul.survey.entity.SurveyResponse;
import com.bosshi.maeul.survey.repository.SurveyResponseRepository;
import com.bosshi.maeul.survey.request.SurveyResponseUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SurveyResponseService {

    private final SurveyResponseRepository responseRepository;
    private final SurveyAnswerFactory answerFactory;

    public SurveyResponse getCurrentResponsesByUserId(Long userId, Long surveyId) {
        return responseRepository.findFirstByUserIdAndSurveyIdOrderByCreatedAtDesc(userId, surveyId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 설문 응답입니다. UserID: " + userId + ", SurveyID: " + surveyId));
    }

    /**
     * 사용자의 현재 응답에 담긴 답변 전체를 요청 내용으로 교체한다.
     * 응답(SurveyResponse)은 유지되고, 기존 답변은 orphanRemoval 로 삭제된다.
     */
    @Transactional
    public SurveyResponse replaceCurrentAnswers(Long userId, Long surveyId, SurveyResponseUpdateRequest request) {
        SurveyResponse surveyResponse = getCurrentResponsesByUserId(userId, surveyId);
        surveyResponse.replaceAnswers(answerFactory.create(surveyResponse.getSurvey(), request.getAnswers()));
        return surveyResponse;
    }
}
