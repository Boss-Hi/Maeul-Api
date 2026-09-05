package com.bosshi.maeul.survey.controller;

import com.bosshi.maeul.common.response.ApiResponse;
import com.bosshi.maeul.common.security.CustomUserDetails;
import com.bosshi.maeul.survey.entity.Survey;
import com.bosshi.maeul.survey.request.SurveySubmitRequest;
import com.bosshi.maeul.survey.response.SurveyResponse;
import com.bosshi.maeul.survey.service.SurveyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/surveys")
@RequiredArgsConstructor
public class SurveyController {
    private final SurveyService surveyService;

    /**
     * 설문 폼 정보 단건 조회
     */
    @GetMapping("/first")
    public ResponseEntity<ApiResponse<SurveyResponse>> first() {
        Survey survey = surveyService.getFirst();
        return ApiResponse.success(SurveyResponse.from(survey));
    }

    /**
     * 설문 답변 제출
     */
    @PostMapping()
    public ResponseEntity<ApiResponse<Long>> submitSurvey(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody SurveySubmitRequest request
    ) {
        com.bosshi.maeul.survey.entity.SurveyResponse surveyResponse = surveyService.submitSurvey(userDetails.getId(), request);
        return ApiResponse.success(surveyResponse.getId());
    }
}