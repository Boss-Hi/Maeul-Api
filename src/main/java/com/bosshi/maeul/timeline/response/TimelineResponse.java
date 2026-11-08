package com.bosshi.maeul.timeline.response;

import com.bosshi.maeul.timeline.dto.TimelineSegment;

import java.time.LocalDate;
import java.util.List;

/**
 * 하루 타임라인. 시각은 timeline.zone(기본 Asia/Seoul) 기준이다.
 */
public record TimelineResponse(
        LocalDate date,
        List<TimelineSegment> segments
) {
}
