package com.bosshi.maeul.itineraries.itinerary.photomission;

public enum AttemptResult {
    SUCCESS,
    /** AI 가 같은 장소가 아니라고 판정 */
    NOT_SAME,
    /** AI 가 같다고 했지만 확신도가 기준 미만 */
    LOW_CONFIDENCE,
    /** EXIF 촬영 시각이 업로드와 너무 멀다 */
    EXIF_STALE
}
