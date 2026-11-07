package com.bosshi.maeul.territory.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 시간순 위치 기록(시군구 판정 포함)으로 점령 시간을 계산한다.
 * <p>
 * 연속된 두 기록이 같은 시군구이고 간격이 maxGap 이하이면 그 간격을 그 시군구의 점령 시간으로 센다.
 * 시군구가 바뀌는 간격, 기록이 끊긴 간격, 지역을 모르는 기록이 낀 간격은 세지 않는다.
 */
public final class OccupationCalculator {

    private OccupationCalculator() {
    }

    /** @param regionId null 이면 지역을 모름(지역 밖, 판정 실패) */
    public record RegionPoint(LocalDateTime recordedAt, String regionId) {
    }

    /**
     * @param confirmedUntil 마지막 위치 기록 시각
     * @param live           마지막 기록이 maxGap 이내라 아직 머무는 중으로 볼 수 있는지
     */
    public record Visit(String regionId, LocalDateTime startedAt, LocalDateTime confirmedUntil,
                        long confirmedStaySeconds, boolean live) {
    }

    /** 시군구별 점령 시간(초) */
    public static Map<String, Long> staySeconds(List<RegionPoint> points, Duration maxGap) {
        Map<String, Long> seconds = new HashMap<>();
        for (int i = 1; i < points.size(); i++) {
            RegionPoint previous = points.get(i - 1);
            RegionPoint current = points.get(i);
            if (counts(previous, current, maxGap)) {
                seconds.merge(current.regionId(), Duration.between(previous.recordedAt(), current.recordedAt()).toSeconds(), Long::sum);
            }
        }
        return seconds;
    }

    /** 마지막 기록이 속한 시군구에 연속으로 머문 구간 */
    public static Optional<Visit> currentVisit(List<RegionPoint> points, Duration maxGap, LocalDateTime now) {
        if (points.isEmpty() || points.getLast().regionId() == null) {
            return Optional.empty();
        }
        int start = points.size() - 1;
        while (start > 0 && counts(points.get(start - 1), points.get(start), maxGap)) {
            start--;
        }
        LocalDateTime startedAt = points.get(start).recordedAt();
        LocalDateTime confirmedUntil = points.getLast().recordedAt();
        return Optional.of(new Visit(
                points.getLast().regionId(), startedAt, confirmedUntil,
                Duration.between(startedAt, confirmedUntil).toSeconds(),
                Duration.between(confirmedUntil, now).compareTo(maxGap) <= 0));
    }

    private static boolean counts(RegionPoint previous, RegionPoint current, Duration maxGap) {
        return current.regionId() != null
                && Objects.equals(previous.regionId(), current.regionId())
                && Duration.between(previous.recordedAt(), current.recordedAt()).compareTo(maxGap) <= 0;
    }
}
