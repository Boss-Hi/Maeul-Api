package com.bosshi.maeul.itinerary.client;

import com.bosshi.maeul.itinerary.dto.SurveyAnswerInfo;

import java.util.List;

/**
 * 사용자의 현재 설문 답변을 가져온다. 응답한 적이 없으면 빈 목록이다.
 */
public interface SurveyAnswerClient {

    /**
     * @throws com.bosshi.maeul.common.exception.ServiceUnavailableException 답변을 가져오지 못했을 때
     */
    List<SurveyAnswerInfo> findAnswers(Long userId);
}
