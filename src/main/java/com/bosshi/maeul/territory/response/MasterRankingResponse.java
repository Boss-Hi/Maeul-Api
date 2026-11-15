package com.bosshi.maeul.territory.response;

import com.bosshi.maeul.territory.dto.MasterRanking;

import java.util.List;

/**
 * 지역별 주인 순위. 그 지역에서 펫을 키우는 중이거나 졸업시킨 사용자를 주인 경험치 전체 순으로 줄 세운다.
 *
 * @param mine 내 순위 (그 지역 펫이 없으면 null)
 */
public record MasterRankingResponse(String regionCode, String code, String regionName, int participantCount,
                                    List<MasterRanking.Entry> entries, MasterRanking.Entry mine) {

    public static MasterRankingResponse from(MasterRanking ranking) {
        return new MasterRankingResponse(ranking.regionCode(), ranking.code(), ranking.regionName(), ranking.participantCount(),
                ranking.entries(), ranking.mine());
    }
}
