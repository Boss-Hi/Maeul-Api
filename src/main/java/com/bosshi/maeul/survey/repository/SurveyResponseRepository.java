package com.bosshi.maeul.survey.repository;

import com.bosshi.maeul.survey.entity.SurveyResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SurveyResponseRepository extends JpaRepository<SurveyResponse, Long> {
    SurveyResponse findByUserIdAndSurveyId(Long userId, Long surveyId);

    // 가장 최근 작성된 응답 1건만 가져오기 (단일 객체 필요 시)
    Optional<SurveyResponse> findFirstByUserIdAndSurveyIdOrderByCreatedAtDesc(Long userId, Long surveyId);
}
