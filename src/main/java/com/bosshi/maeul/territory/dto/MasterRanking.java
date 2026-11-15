package com.bosshi.maeul.territory.dto;

import java.util.List;

/**
 * 지역별 주인 순위. 그 지역에서 펫을 키우는 중이거나 졸업시킨 사용자를 주인 경험치 전체 순으로 줄 세운다.
 *
 * @param regionCode       시도 코드 (전국이면 null)
 * @param code             시군구 코드 (시도·전국이면 null)
 * @param participantCount 순위에 든 주인 수
 * @param mine             내 순위 (그 지역 펫이 없으면 null)
 */
public record MasterRanking(String regionCode, String code, String regionName, int participantCount,
                            List<Entry> entries, Entry mine) {

    public record Entry(int rank, String nickname, int level, long exp, boolean mine) {
    }
}
