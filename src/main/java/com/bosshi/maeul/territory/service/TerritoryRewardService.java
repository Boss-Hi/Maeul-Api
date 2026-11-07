package com.bosshi.maeul.territory.service;

import com.bosshi.maeul.point.service.PointService;
import com.bosshi.maeul.point.type.PointReason;
import com.bosshi.maeul.territory.config.TerritoryProperties;
import com.bosshi.maeul.territory.repository.TerritoryDailyStayRepository;
import com.bosshi.maeul.territory.response.MyTerritoryResponse.RewardProgress;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 전체 점령 시간이 rewardUnit 을 넘길 때마다 rewardPoints 를 준다.
 * 지급 키(OCCUPATION:n)로 같은 보상을 두 번 주지 않으며, 위치 기록을 지워도 준 포인트는 회수하지 않는다.
 */
@Service
@RequiredArgsConstructor
public class TerritoryRewardService {

    private final TerritoryDailyStayRepository dailyStayRepository;
    private final PointService pointService;
    private final TerritoryProperties properties;

    @Transactional
    public void reward(Long userId) {
        long earnedUnits = dailyStayRepository.sumByUser(userId) / unitSeconds();
        long awardedUnits = pointService.count(userId, PointReason.OCCUPATION);
        for (long unit = awardedUnits + 1; unit <= earnedUnits; unit++) {
            pointService.award(userId, properties.getRewardPoints(), PointReason.OCCUPATION, PointReason.OCCUPATION.name() + ":" + unit);
        }
    }

    public RewardProgress progress(Long userId) {
        long unitSeconds = unitSeconds();
        long awardedUnits = pointService.count(userId, PointReason.OCCUPATION);
        long progress = Math.clamp(dailyStayRepository.sumByUser(userId) - awardedUnits * unitSeconds, 0, unitSeconds);
        return new RewardProgress(unitSeconds, properties.getRewardPoints(), awardedUnits, progress, unitSeconds - progress);
    }

    private long unitSeconds() {
        return properties.getRewardUnit().toSeconds();
    }
}
