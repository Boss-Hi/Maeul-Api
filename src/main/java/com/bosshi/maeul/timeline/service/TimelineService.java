package com.bosshi.maeul.timeline.service;

import com.bosshi.maeul.timeline.config.TimelineProperties;
import com.bosshi.maeul.timeline.entity.TimelinePoint;
import com.bosshi.maeul.timeline.repository.TimelinePointRepository;
import com.bosshi.maeul.timeline.request.TimelinePointsRequest;
import com.bosshi.maeul.timeline.response.TimelineDeleteResponse;
import com.bosshi.maeul.timeline.response.TimelineRecordResponse;
import com.bosshi.maeul.timeline.response.TimelineResponse;
import com.bosshi.maeul.timeline.response.TimelineResponse.TimelinePosition;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TimelineService {

    /** 기기 시계가 서버보다 빠를 수 있는 허용치 */
    private static final Duration ALLOWED_CLOCK_SKEW = Duration.ofMinutes(1);

    private final TimelinePointRepository pointRepository;
    private final TimelineProperties properties;

    /**
     * 위치 기록 저장. 수집 주기보다 촘촘한 기록, 이미 저장된 시각, 미래 시각의 기록은 버린다.
     */
    @Transactional
    public TimelineRecordResponse record(Long userId, TimelinePointsRequest request) {
        List<TimelinePoint> incoming = request.points().stream()
                .map(point -> TimelinePoint.builder()
                        .userId(userId)
                        .latitude(point.latitude())
                        .longitude(point.longitude())
                        .accuracy(point.accuracy())
                        .recordedAt(point.recordedAt().atZoneSameInstant(properties.zoneId()).toLocalDateTime())
                        .build())
                .toList();

        Duration minimumGap = properties.minimumGap();
        LocalDateTime earliest = incoming.stream().map(TimelinePoint::getRecordedAt).min(Comparator.naturalOrder()).orElseThrow();
        LocalDateTime latest = incoming.stream().map(TimelinePoint::getRecordedAt).max(Comparator.naturalOrder()).orElseThrow();
        List<LocalDateTime> stored = pointRepository
                .findByUserIdAndRecordedAtGreaterThanEqualAndRecordedAtLessThanOrderByRecordedAtAsc(
                        userId, earliest.minus(minimumGap), latest.plus(minimumGap).plusNanos(1))
                .stream()
                .map(TimelinePoint::getRecordedAt)
                .toList();

        LocalDateTime latestAllowed = LocalDateTime.now(properties.zoneId()).plus(ALLOWED_CLOCK_SKEW);
        List<TimelinePoint> accepted = TimelinePointSampler.sample(
                incoming, TimelinePoint::getRecordedAt, stored, minimumGap, latestAllowed);
        pointRepository.saveAll(accepted);

        return new TimelineRecordResponse(accepted.size(), incoming.size() - accepted.size());
    }

    /**
     * 하루 타임라인 조회
     */
    public TimelineResponse getDay(Long userId, LocalDate date) {
        List<TimelinePosition> positions = pointRepository
                .findByUserIdAndRecordedAtGreaterThanEqualAndRecordedAtLessThanOrderByRecordedAtAsc(
                        userId, date.atStartOfDay(), date.plusDays(1).atStartOfDay())
                .stream()
                .map(point -> new TimelinePosition(point.getLatitude(), point.getLongitude(), point.getRecordedAt()))
                .toList();

        return new TimelineResponse(date, TimelineSegmenter.segment(
                positions, properties.getStayRadiusMeters(), properties.getMinStayDuration()));
    }

    /**
     * 하루 위치 기록 삭제 (복구 불가)
     */
    @Transactional
    public TimelineDeleteResponse deleteDay(Long userId, LocalDate date) {
        int deleted = pointRepository.deleteByUserIdAndRange(userId, date.atStartOfDay(), date.plusDays(1).atStartOfDay());
        return new TimelineDeleteResponse(date, deleted);
    }
}
