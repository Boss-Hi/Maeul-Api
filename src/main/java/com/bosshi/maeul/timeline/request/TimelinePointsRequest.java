package com.bosshi.maeul.timeline.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * 기기에서 모아 둔 위치 기록 업로드 (오프라인 동안 쌓인 기록을 한 번에 보낼 수 있음)
 */
public record TimelinePointsRequest(
        @NotEmpty(message = "위치 기록은 1개 이상이어야 합니다.")
        @Size(max = 1440, message = "위치 기록은 한 번에 1440개까지 보낼 수 있습니다.")
        List<@NotNull(message = "위치 기록은 null 일 수 없습니다.") @Valid PointRequest> points
) {
    public record PointRequest(
            @NotNull(message = "위도는 필수입니다.")
            @DecimalMin(value = "-90", message = "위도는 -90 이상이어야 합니다.")
            @DecimalMax(value = "90", message = "위도는 90 이하이어야 합니다.")
            Double latitude,

            @NotNull(message = "경도는 필수입니다.")
            @DecimalMin(value = "-180", message = "경도는 -180 이상이어야 합니다.")
            @DecimalMax(value = "180", message = "경도는 180 이하이어야 합니다.")
            Double longitude,

            @PositiveOrZero(message = "정확도는 0 이상이어야 합니다.")
            Double accuracy,

            /** 수집 시각 (ISO-8601, 시간대 포함. 예: 2026-11-07T09:00:00+09:00) */
            @NotNull(message = "수집 시각은 필수입니다.")
            OffsetDateTime recordedAt
    ) {
    }
}
