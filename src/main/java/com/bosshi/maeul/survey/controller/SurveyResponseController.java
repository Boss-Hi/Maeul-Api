package com.bosshi.maeul.survey.controller;

import com.bosshi.maeul.auth.CustomUserDetails;
import com.bosshi.maeul.common.response.ApiResponse;
import com.bosshi.maeul.survey.entity.Survey;
import com.bosshi.maeul.survey.entity.SurveyResponse;
import com.bosshi.maeul.survey.request.SurveyResponseUpdateRequest;
import com.bosshi.maeul.survey.response.SurveyResResponse;
import com.bosshi.maeul.survey.service.SurveyResponseService;
import com.bosshi.maeul.survey.service.SurveyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/surveys")
@RequiredArgsConstructor
public class SurveyResponseController {
    private final SurveyResponseService surveyResponseService;
    private final SurveyService surveyService;

    /**
     * 등록한 설문 단건 조회
     */
    @GetMapping("/responses/current")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<SurveyResResponse>> showStoredResponse(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Survey survey = surveyService.getFirst();
        SurveyResponse surveyResponse = surveyResponseService.getCurrentResponsesByUserId(userDetails.getId(), survey.getId());
        return ApiResponse.success(SurveyResResponse.from(surveyResponse));
    }

    /**
     * 등록한 설문 수정
     */
    @PutMapping("/responses/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<SurveyResResponse>> updateStoredResponse(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long id, @RequestBody SurveyResponseUpdateRequest request) {
        SurveyResponse surveyResponse = surveyResponseService.updateBySurveyResponseId(id, request);
        return ApiResponse.success(SurveyResResponse.from(surveyResponse));
    }
}