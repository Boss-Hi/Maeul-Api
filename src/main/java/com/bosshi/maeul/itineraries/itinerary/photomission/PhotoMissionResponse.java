package com.bosshi.maeul.itineraries.itinerary.photomission;

import java.time.LocalDate;
import java.util.List;

/**
 * 퀴즈로 보여 줄 기준 사진들과 진행 상태. 기준 사진 중 하나라도 같은 지점을 찍으면 성공이다.
 * imageUrl 이 / 로 시작하면 이 서버의 경로이므로 API 주소를 앞에 붙인다.
 */
public record PhotoMissionResponse(Long id, String placeName, List<ReferencePhotoResponse> referencePhotos, PhotoMissionStatus status,
                                   int remainingAttempts, LocalDate startDate, LocalDate endDate) {

    public record ReferencePhotoResponse(Long id, String imageUrl) {
    }

    static PhotoMissionResponse from(PhotoMissionService.MissionView view) {
        return new PhotoMissionResponse(view.mission().getId(), view.place().getName(),
                view.referencePhotos().stream().map(p -> new ReferencePhotoResponse(p.id(), p.imageUrl())).toList(),
                view.mission().getStatus(), view.remainingAttempts(), view.mission().getStartDate(), view.mission().getEndDate());
    }
}
