package com.bosshi.maeul.survey.repository;

import com.bosshi.maeul.survey.entity.Survey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SurveyRepository extends JpaRepository<Survey, Long> {
    java.util.Optional<Survey> findByCode(String code);
    java.util.Optional<Survey> findByCodeAndActiveIsTrue(String code);
}
