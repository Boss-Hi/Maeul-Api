package com.bosshi.maeul.itineraries.itinerary.photomission;

/**
 * 사진 제출 결과. 실패 사유는 기준 사진을 유추하게 하므로 일반 문구만 준다.
 *
 * @param ticketIssued 가챠권 1장을 받았는가
 */
public record PhotoMissionSubmitResponse(boolean success, String message, int remainingAttempts, boolean ticketIssued) {

    static PhotoMissionSubmitResponse from(PhotoMissionService.SubmitResult result) {
        String message = switch (result.result()) {
            case SUCCESS -> result.ticketIssued() ? "미션 성공! 가챠권 1장을 받았어요." : "미션 성공!";
            case EXIF_STALE -> "방금 촬영한 사진을 올려 주세요.";
            case NOT_SAME, LOW_CONFIDENCE -> "같은 장소로 보이지 않아요. 기준 사진과 같은 곳을 다시 찍어 보세요.";
        };
        return new PhotoMissionSubmitResponse(result.success(), message, result.remainingAttempts(), result.ticketIssued());
    }
}
