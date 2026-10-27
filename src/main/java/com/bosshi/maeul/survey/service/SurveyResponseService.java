package com.bosshi.maeul.survey.service;

import com.bosshi.maeul.survey.entity.SurveyAnswer;
import com.bosshi.maeul.survey.entity.SurveyQuestionOption;
import com.bosshi.maeul.survey.entity.SurveyResponse;
import com.bosshi.maeul.survey.repository.SurveyQuestionOptionRepository;
import com.bosshi.maeul.survey.repository.SurveyResponseRepository;
import com.bosshi.maeul.survey.request.SurveyResponseUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SurveyResponseService {

    private final SurveyResponseRepository responseRepository;
    private final SurveyQuestionOptionRepository optionRepository;

    public SurveyResponse getCurrentResponsesByUserId(Long userId, Long surveyId) {
        return responseRepository.findFirstByUserIdAndSurveyIdOrderByCreatedAtDesc(userId, surveyId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 설문 응답입니다. UserID: " + userId + ", SurveyID: " + surveyId));
    }

    @Transactional
    public SurveyResponse updateBySurveyResponseId(Long surveyResponseId, SurveyResponseUpdateRequest request) {
        // 1. SurveyResponse 엔티티 조회 (연관된 answers 함께 로딩)
        SurveyResponse surveyResponse = responseRepository.findById(surveyResponseId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 설문 응답입니다. ID: " + surveyResponseId));

        // 2. 요청으로 들어온 optionId 목록을 추출하여 한 번에 Option 엔티티들을 조회 (N+1 방지)
        Set<Long> optionIds = request.getAnswers().stream()
                .map(SurveyResponseUpdateRequest.AnswerUpdateRequest::getOptionId)
                .collect(Collectors.toSet());

        Map<Long, SurveyQuestionOption> optionMap = optionRepository.findAllById(optionIds).stream()
                .collect(Collectors.toMap(SurveyQuestionOption::getId, Function.identity()));

        // 3. 기존 SurveyAnswer 엔티티들을 Map으로 구성 (ID -> SurveyAnswer)
        Map<Long, SurveyAnswer> existingAnswerMap = surveyResponse.getAnswers().stream()
                .collect(Collectors.toMap(SurveyAnswer::getId, Function.identity()));

        // 4. 요청 데이터를 순회하며 기존 SurveyAnswer의 Option 값 업데이트
        for (SurveyResponseUpdateRequest.AnswerUpdateRequest answerReq : request.getAnswers()) {
            SurveyAnswer existingAnswer = existingAnswerMap.get(answerReq.getId());

            if (existingAnswer == null) {
                throw new IllegalArgumentException("해당 설문 응답에 존재하지 않는 답변 ID입니다. ID: " + answerReq.getId());
            }

            SurveyQuestionOption newOption = optionMap.get(answerReq.getOptionId());
            if (newOption == null) {
                throw new IllegalArgumentException("존재하지 않는 옵션 ID입니다. ID: " + answerReq.getOptionId());
            }

            // JPA 변경 감지(Dirty Checking)를 통한 Option 변경
            existingAnswer.updateOption(newOption);
        }

        // @Transactional 범위 내이므로 save() 호출 필요 없이 트랜잭션 종료 시 UPDATE SQL 자동 실행
        return surveyResponse;
    }
}