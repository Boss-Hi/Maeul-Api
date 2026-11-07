package com.bosshi.maeul.timeline.service;

import com.bosshi.maeul.timeline.response.TimelineResponse.TimelinePosition;
import com.bosshi.maeul.timeline.response.TimelineResponse.TimelineSegment;
import com.bosshi.maeul.timeline.type.TimelineSegmentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.bosshi.maeul.timeline.type.TimelineSegmentType.MOVE;
import static com.bosshi.maeul.timeline.type.TimelineSegmentType.STAY;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TimelineSegmenter 테스트")
class TimelineSegmenterTest {

    private static final LocalDateTime BASE = LocalDateTime.of(2026, 11, 7, 9, 0);
    private static final double RADIUS = 100;
    private static final Duration MIN_STAY = Duration.ofMinutes(5);

    // A(광주 충장로 부근) 에서 동쪽으로 약 2km 떨어진 B
    private static final double A_LAT = 35.1468, A_LNG = 126.9190;
    private static final double B_LAT = 35.1468, B_LNG = 126.9410;

    @Test
    @DisplayName("머문 장소 - 이동 - 머문 장소로 나누고 이동 거리를 경로 합으로 구한다")
    void staysAndMove() {
        Track track = new Track()
                .stay(A_LAT, A_LNG, 10)
                .moveTo(B_LAT, B_LNG, 6)
                .stay(B_LAT, B_LNG, 10);

        List<TimelineSegment> segments = segment(track);

        assertThat(segments).extracting(TimelineSegment::type).containsExactly(STAY, MOVE, STAY);
        TimelineSegment stayA = segments.get(0);
        assertThat(stayA.startAt()).isEqualTo(BASE);
        assertThat(stayA.endAt()).isEqualTo(BASE.plusMinutes(9));
        assertThat(stayA.pointCount()).isEqualTo(10);

        TimelineSegment move = segments.get(1);
        assertThat(move.startAt()).isEqualTo(stayA.endAt());
        assertThat(move.endAt()).isEqualTo(segments.get(2).startAt());
        assertThat(move.distanceMeters()).isBetween(1900L, 2100L);
        assertThat(move.path()).hasSize(8); // 이동 중 6개 + 앞뒤 STAY 경계 2개
    }

    @Test
    @DisplayName("최소 체류 시간보다 짧게 멈춘 곳은 이동에 포함한다")
    void shortStopIsPartOfMove() {
        Track track = new Track()
                .stay(A_LAT, A_LNG, 6)
                .moveTo(35.1468, 126.9300, 3)
                .stay(35.1468, 126.9300, 3) // 2분 정차
                .moveTo(B_LAT, B_LNG, 3)
                .stay(B_LAT, B_LNG, 6);

        assertThat(segment(track)).extracting(TimelineSegment::type).containsExactly(STAY, MOVE, STAY);
    }

    @Test
    @DisplayName("머무는 중 GPS 가 한 번 튀어도 하나의 머문 장소로 본다")
    void gpsJumpInsideStayIsMerged() {
        Track track = new Track()
                .stay(A_LAT, A_LNG, 6)
                .stay(A_LAT + 0.005, A_LNG, 1) // 약 550m 튐
                .stay(A_LAT, A_LNG, 6);

        List<TimelineSegment> segments = segment(track);

        assertThat(segments).extracting(TimelineSegment::type).containsExactly(STAY);
        assertThat(segments.getFirst().pointCount()).isEqualTo(13);
        assertThat(segments.getFirst().endAt()).isEqualTo(BASE.plusMinutes(12));
    }

    @Test
    @DisplayName("최소 체류 시간 이상 다른 곳에 다녀오면 같은 장소라도 따로 본다")
    void longTripReturningHomeIsNotMerged() {
        Track track = new Track()
                .stay(A_LAT, A_LNG, 6)
                .moveTo(B_LAT, B_LNG, 4)
                .moveTo(A_LAT, A_LNG, 4)
                .stay(A_LAT, A_LNG, 6);

        assertThat(segment(track)).extracting(TimelineSegment::type).containsExactly(STAY, MOVE, STAY);
    }

    @Test
    @DisplayName("기록이 없거나 1개뿐이면 구간을 만들지 않는다")
    void notEnoughPoints() {
        assertThat(TimelineSegmenter.segment(List.of(), RADIUS, MIN_STAY)).isEmpty();
        assertThat(TimelineSegmenter.segment(List.of(new TimelinePosition(A_LAT, A_LNG, BASE)), RADIUS, MIN_STAY)).isEmpty();
    }

    @Test
    @DisplayName("이동만 있으면 이동 구간 하나가 된다")
    void onlyMoving() {
        Track track = new Track().stay(A_LAT, A_LNG, 1).moveTo(B_LAT, B_LNG, 5);

        List<TimelineSegment> segments = segment(track);

        assertThat(segments).extracting(TimelineSegment::type).containsExactly(TimelineSegmentType.MOVE);
        assertThat(segments.getFirst().pointCount()).isEqualTo(6);
    }

    private List<TimelineSegment> segment(Track track) {
        return TimelineSegmenter.segment(track.points, RADIUS, MIN_STAY);
    }

    /** 1분 간격으로 기록을 쌓는 테스트용 궤적 */
    private static class Track {
        private final List<TimelinePosition> points = new ArrayList<>();
        private double lat, lng;

        Track stay(double latitude, double longitude, int minutes) {
            for (int k = 0; k < minutes; k++) add(latitude, longitude);
            return this;
        }

        /** 현재 위치에서 목적지까지 minutes 개 기록으로 고르게 이동 (마지막 기록은 목적지 직전) */
        Track moveTo(double latitude, double longitude, int minutes) {
            double fromLat = lat, fromLng = lng;
            for (int k = 1; k <= minutes; k++) {
                double ratio = (double) k / (minutes + 1);
                add(fromLat + (latitude - fromLat) * ratio, fromLng + (longitude - fromLng) * ratio);
            }
            return this;
        }

        private void add(double latitude, double longitude) {
            points.add(new TimelinePosition(latitude, longitude, BASE.plusMinutes(points.size())));
            lat = latitude;
            lng = longitude;
        }
    }
}
