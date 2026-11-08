package com.bosshi.maeul.timeline.service;

import com.bosshi.maeul.location.service.GeoService;
import com.bosshi.maeul.timeline.dto.TimelinePosition;
import com.bosshi.maeul.timeline.dto.TimelineSegment;
import com.bosshi.maeul.timeline.type.TimelineSegmentType;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 시간순 위치 기록을 머문 장소(STAY)와 이동(MOVE) 구간으로 나눈다.
 * <ul>
 *     <li>STAY: 연속된 기록이 중심점에서 radiusMeters 안에 minStay 이상 머문 구간</li>
 *     <li>MOVE: STAY 사이(또는 앞뒤)의 구간. 경로와 이동 거리를 가진다</li>
 * </ul>
 * 같은 장소의 STAY 두 개가 minStay 보다 짧은 튐(GPS 오차)으로 나뉘면 하나로 합친다.
 */
public final class TimelineSegmenter {

    private TimelineSegmenter() {
    }

    public static List<TimelineSegment> segment(List<TimelinePosition> points, double radiusMeters, Duration minStay) {
        List<Object> parts = new ArrayList<>(); // Stay 또는 List<TimelinePosition>(이동 중 기록)
        List<TimelinePosition> moving = new ArrayList<>();
        Stay lastStay = null;

        int i = 0;
        while (i < points.size()) {
            Stay cluster = new Stay(points.get(i));
            int j = i;
            while (j + 1 < points.size() && distanceMeters(cluster.center(), points.get(j + 1)) <= radiusMeters) {
                j++;
                cluster.add(points.get(j));
            }

            if (Duration.between(cluster.first.recordedAt(), cluster.last.recordedAt()).compareTo(minStay) < 0) {
                moving.add(points.get(i));
                i++;
                continue;
            }

            if (lastStay != null
                    && distanceMeters(lastStay.center(), cluster.center()) <= radiusMeters
                    && Duration.between(lastStay.last.recordedAt(), cluster.first.recordedAt()).compareTo(minStay) < 0) {
                // minStay 보다 짧게 벗어났다 같은 장소로 돌아옴(GPS 튐): 사이 기록까지 이전 STAY 에 합친다
                moving.forEach(lastStay::add);
                cluster.points.forEach(lastStay::add);
            } else {
                if (!moving.isEmpty() || lastStay != null) {
                    parts.add(new ArrayList<>(moving));
                }
                parts.add(cluster);
                lastStay = cluster;
            }
            moving.clear();
            i = j + 1;
        }
        if (!moving.isEmpty()) {
            parts.add(new ArrayList<>(moving));
        }

        List<TimelineSegment> segments = new ArrayList<>();
        for (int k = 0; k < parts.size(); k++) {
            if (parts.get(k) instanceof Stay stay) {
                segments.add(stay.toSegment());
                continue;
            }
            @SuppressWarnings("unchecked")
            List<TimelinePosition> path = new ArrayList<>((List<TimelinePosition>) parts.get(k));
            if (k > 0 && parts.get(k - 1) instanceof Stay previous) {
                path.addFirst(previous.last);
            }
            if (k + 1 < parts.size() && parts.get(k + 1) instanceof Stay next) {
                path.addLast(next.first);
            }
            if (path.size() >= 2) {
                segments.add(move(path));
            }
        }
        return segments;
    }

    private static TimelineSegment move(List<TimelinePosition> path) {
        double distance = 0;
        for (int k = 1; k < path.size(); k++) {
            distance += distanceMeters(path.get(k - 1), path.get(k));
        }
        LocalDateTime startAt = path.getFirst().recordedAt();
        LocalDateTime endAt = path.getLast().recordedAt();
        return new TimelineSegment(
                TimelineSegmentType.MOVE, startAt, endAt, Duration.between(startAt, endAt).toSeconds(),
                null, null, path.size(), Math.round(distance), List.copyOf(path));
    }

    private static double distanceMeters(TimelinePosition a, TimelinePosition b) {
        return GeoService.calculateDistance(a.latitude(), a.longitude(), b.latitude(), b.longitude()) * 1000;
    }

    private static final class Stay {
        private final List<TimelinePosition> points = new ArrayList<>();
        private TimelinePosition first;
        private TimelinePosition last;
        private double latitudeSum;
        private double longitudeSum;

        private Stay(TimelinePosition point) {
            add(point);
        }

        private void add(TimelinePosition point) {
            if (first == null || point.recordedAt().isBefore(first.recordedAt())) first = point;
            if (last == null || !point.recordedAt().isBefore(last.recordedAt())) last = point;
            points.add(point);
            latitudeSum += point.latitude();
            longitudeSum += point.longitude();
        }

        private TimelinePosition center() {
            return new TimelinePosition(latitudeSum / points.size(), longitudeSum / points.size(), null);
        }

        private TimelineSegment toSegment() {
            TimelinePosition center = center();
            return new TimelineSegment(
                    TimelineSegmentType.STAY, first.recordedAt(), last.recordedAt(),
                    Duration.between(first.recordedAt(), last.recordedAt()).toSeconds(),
                    center.latitude(), center.longitude(), points.size(), null, null);
        }
    }
}
