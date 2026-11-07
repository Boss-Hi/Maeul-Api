package com.bosshi.maeul.territory.response;

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
    public record Entry(int rank, String nickname, long staySeconds, boolean me) {
    }
}
