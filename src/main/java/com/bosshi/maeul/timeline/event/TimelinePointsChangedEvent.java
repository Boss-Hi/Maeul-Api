package com.bosshi.maeul.timeline.event;

import java.time.LocalDate;
import java.util.Set;

/**
 * 사용자의 위치 기록이 저장되거나 삭제된 날짜들 (timeline.zone 기준)
 */
public record TimelinePointsChangedEvent(Long userId, Set<LocalDate> dates) {
}
