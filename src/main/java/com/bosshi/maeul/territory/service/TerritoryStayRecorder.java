package com.bosshi.maeul.territory.service;

import com.bosshi.maeul.territory.config.TerritoryProperties;
import com.bosshi.maeul.territory.entity.TerritoryDailyStay;
import com.bosshi.maeul.territory.repository.TerritoryDailyStayRepository;
import com.bosshi.maeul.timeline.event.TimelinePointsChangedEvent;
import com.bosshi.maeul.timeline.repository.TimelinePointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 위치 기록이 바뀐 날짜의 점령 시간을 위치 기록에서 다시 계산하고, 점령 보상을 지급한다.
 * 같은 날짜를 몇 번 다시 계산해도 결과가 같다.
 */
@Component
@RequiredArgsConstructor
public class TerritoryStayRecorder {

    private final TimelinePointRepository pointRepository;
    private final TerritoryDailyStayRepository dailyStayRepository;
    private final TerritoryPositionResolver positionResolver;
    private final TerritoryRewardService rewardService;
    private final TerritoryProperties properties;

    @EventListener
    @Transactional
    public void onPointsChanged(TimelinePointsChangedEvent event) {
        event.dates().forEach(date -> recompute(event.userId(), date));
        rewardService.reward(event.userId());
    }

    private void recompute(Long userId, LocalDate date) {
        List<OccupationCalculator.RegionPoint> points = positionResolver.resolve(
                pointRepository.findByUserIdAndRecordedAtGreaterThanEqualAndRecordedAtLessThanOrderByRecordedAtAsc(
                        userId, date.atStartOfDay(), date.plusDays(1).atStartOfDay()));
        Map<String, Long> seconds = OccupationCalculator.staySeconds(points, properties.getMaxGap());

        dailyStayRepository.deleteByUserIdAndStayDate(userId, date);
        dailyStayRepository.saveAll(seconds.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .map(entry -> TerritoryDailyStay.builder()
                        .userId(userId)
                        .regionId(entry.getKey())
                        .stayDate(date)
                        .staySeconds(entry.getValue())
                        .build())
                .toList());
    }
}
