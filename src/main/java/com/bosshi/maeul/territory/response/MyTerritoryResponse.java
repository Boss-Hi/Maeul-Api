package com.bosshi.maeul.territory.response;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

/**
 * 내 점령 현황
 *
 * @param rankingMonth 월간 순위 기준 월
 * @param currentVisit 지금 머무는 시군구 (마지막 위치 기록 기준, 없으면 null)
 * @param regions      점령한 시군구 (누적 점령 시간 내림차순)
 */
public record MyTerritoryResponse(
        YearMonth rankingMonth,
        CurrentVisit currentVisit,
        List<RegionStat> regions,
        RewardProgress rewardProgress,
        Points points
) {
    /**
     * @param confirmedUntil       마지막 위치 기록 시각. 이후 시간은 앱이 타이머로 표시한다
     * @param confirmedStaySeconds startedAt ~ confirmedUntil (이미 regions 의 누적 시간에 포함됨)
     * @param live                 마지막 기록이 끊긴 지 territory.max-gap 이내인지. false 면 미확인
     */
    public record CurrentVisit(
            String regionId,
            String regionName,
            LocalDateTime startedAt,
            LocalDateTime confirmedUntil,
            long confirmedStaySeconds,
            boolean live
    ) {
    }

    /**
     * @param totalStaySeconds   누적 점령 시간
     * @param personalOrder      내 시군구 중 누적 점령 시간 순서 (다른 사용자와의 순위 아님)
     * @param monthlyStaySeconds rankingMonth 의 점령 시간
     * @param monthlyRank        rankingMonth 에 이 시군구 사용자 중 내 순위 (그달 기록이 없으면 null)
     * @param participantCount   rankingMonth 에 이 시군구 점령 기록이 있는 사용자 수
     */
    public record RegionStat(
            String regionId,
            String name,
            String parentRegionId,
            String parentName,
            long totalStaySeconds,
            int personalOrder,
            long monthlyStaySeconds,
            Integer monthlyRank,
            int participantCount
    ) {
    }

    /**
     * @param unitSeconds      이 시간마다 포인트 적립
     * @param unitPoints       1회 적립 포인트
     * @param earnedUnits      지금까지 적립 횟수
     * @param progressSeconds  다음 적립까지 쌓인 시간
     * @param remainingSeconds 다음 적립까지 남은 시간
     */
    public record RewardProgress(
            long unitSeconds,
            long unitPoints,
            long earnedUnits,
            long progressSeconds,
            long remainingSeconds
    ) {
    }

    public record Points(long balance) {
    }
}
