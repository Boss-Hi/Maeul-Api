package com.bosshi.maeul.itineraries.itinerary.photomission;

/** 기준 사진 목록용 요약. 이미지 본문(base64)을 읽지 않는다 */
public record ReferencePhotoSummary(Long id, String imageUrl, String mimeType) {

    /** 서버가 이미지 본문을 가지고 있는가 (없으면 imageUrl 로 등록한 사진) */
    boolean hasData() {
        return mimeType != null;
    }
}
