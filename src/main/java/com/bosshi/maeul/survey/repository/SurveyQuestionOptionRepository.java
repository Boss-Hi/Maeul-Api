package com.bosshi.maeul.survey.repository;

import com.bosshi.maeul.survey.entity.SurveyQuestionOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SurveyQuestionOptionRepository extends JpaRepository<SurveyQuestionOption, Long> {
}
