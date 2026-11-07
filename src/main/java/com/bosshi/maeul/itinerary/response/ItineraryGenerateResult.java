package com.bosshi.maeul.itinerary.response;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 일정 생성 결과 웹소켓 메시지 (/private/queue/itinerary-generate)
 * 성공: { "status": "SUCCESS", "itineraryId": 1 }
 * 실패: { "status": "FAILED", "message": "..." }
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ItineraryGenerateResult(Status status, Long itineraryId, String message) {

    public enum Status {
        SUCCESS, FAILED
    }

    public static ItineraryGenerateResult success(Long itineraryId) {
        return new ItineraryGenerateResult(Status.SUCCESS, itineraryId, null);
    }

    public static ItineraryGenerateResult failed(String message) {
        return new ItineraryGenerateResult(Status.FAILED, null, message);
    }
}
