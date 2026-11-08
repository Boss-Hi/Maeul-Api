package com.bosshi.maeul.territory.response;

import com.bosshi.maeul.territory.dto.MyTerritory;
import com.bosshi.maeul.territory.dto.MyTerritory.CurrentVisit;
import com.bosshi.maeul.territory.dto.MyTerritory.RegionStat;
import com.bosshi.maeul.territory.dto.MyTerritory.RewardProgress;

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
    public static MyTerritoryResponse from(MyTerritory territory) {
        return new MyTerritoryResponse(territory.rankingMonth(), territory.currentVisit(), territory.regions(),
                territory.rewardProgress(), new Points(territory.pointBalance()));
    }

    public record Points(long balance) {
    }
}
