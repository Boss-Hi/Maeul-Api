package com.bosshi.maeul.timeline.dto;

import com.bosshi.maeul.timeline.type.TimelineSegmentType;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @param latitude       STAY 의 중심 위도
 * @param longitude      STAY 의 중심 경도
 * @param distanceMeters MOVE 의 이동 거리 (경로 합)
 * @param path           MOVE 의 경로 (앞뒤 STAY 의 경계 기록 포함)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TimelineSegment(
        TimelineSegmentType type,
        LocalDateTime startAt,
        LocalDateTime endAt,
        long durationSeconds,
        Double latitude,
        Double longitude,
        int pointCount,
        Long distanceMeters,
        List<TimelinePosition> path
) {
}
