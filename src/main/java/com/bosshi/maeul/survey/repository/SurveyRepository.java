package com.bosshi.maeul.survey.repository;

import com.bosshi.maeul.survey.entity.Survey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SurveyRepository extends JpaRepository<Survey, Long> {
    Optional<Survey> findByCode(String code);
    Optional<Survey> findByCodeAndActiveIsTrue(String code);
}
