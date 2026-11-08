package com.bosshi.maeul.territory.response;

import com.bosshi.maeul.territory.dto.TerritoryRankingResult;
import com.bosshi.maeul.territory.dto.TerritoryRankingResult.Entry;

import java.time.YearMonth;
import java.util.List;

/**
 * 한 시군구의 월간 점령 순위
 *
 * @param participantCount 그달 이 시군구 점령 기록이 있는 사용자 수
 * @param myRank           내 순위 (기록이 없으면 null)
 */
public record TerritoryRankingResponse(
        String regionId,
        String regionName,
        YearMonth rankingMonth,
        int participantCount,
        List<Entry> rankings,
        Entry myRank
) {
    public static TerritoryRankingResponse from(TerritoryRankingResult result) {
        return new TerritoryRankingResponse(result.regionId(), result.regionName(), result.rankingMonth(),
                result.participantCount(), result.rankings(), result.myRank());
    }
}
