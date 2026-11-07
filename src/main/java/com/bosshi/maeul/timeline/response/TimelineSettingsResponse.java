package com.bosshi.maeul.timeline.response;

import com.bosshi.maeul.timeline.config.TimelineProperties;

/**
 * 앱이 위치 수집 주기를 맞추기 위한 설정 값
 */
public record TimelineSettingsResponse(
        long collectionIntervalSeconds,
        double stayRadiusMeters,
        long minStaySeconds,
        String zone
) {
    public static TimelineSettingsResponse from(TimelineProperties properties) {
        return new TimelineSettingsResponse(
                properties.getCollectionInterval().toSeconds(),
                properties.getStayRadiusMeters(),
                properties.getMinStayDuration().toSeconds(),
                properties.getZone());
    }
}
