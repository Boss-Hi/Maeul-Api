package com.bosshi.maeul.timeline.response;

import com.bosshi.maeul.timeline.type.TimelineSegmentType;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 하루 타임라인. 시각은 timeline.zone(기본 Asia/Seoul) 기준이다.
 */
public record TimelineResponse(
        LocalDate date,
        List<TimelineSegment> segments
) {
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

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record TimelinePosition(
            double latitude,
            double longitude,
            LocalDateTime recordedAt
    ) {
    }
}
