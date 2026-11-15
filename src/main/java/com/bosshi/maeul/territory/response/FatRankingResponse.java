package com.bosshi.maeul.territory.response;

import com.bosshi.maeul.territory.dto.FatRanking;

import org.springframework.data.domain.Page;

/**
 * 지역별 펫 순위 (키우는 중인 펫의 지금 경험치 순, 같은 경험치는 같은 순위)
 *
 * @param regionName       "전국", 시도 이름, 또는 "시도 시군구"
 * @param participantCount 순위에 든 펫 수
 * @param entries          요청한 페이지의 순위
 * @param mine             내 펫 중 가장 높은 순위 (없으면 null)
 */
public record FatRankingResponse(String regionCode, String code, String regionName, int participantCount,
                                 Page<FatRanking.Entry> entries, FatRanking.Entry mine) {

    public static FatRankingResponse from(FatRanking ranking) {
        return new FatRankingResponse(ranking.regionCode(), ranking.code(), ranking.regionName(), ranking.participantCount(),
                ranking.entries(), ranking.mine());
    }
}
