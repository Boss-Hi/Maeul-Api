package com.bosshi.maeul.territory.dto;

import java.util.List;

/**
 * 지역별 펫 순위 (키우는 중인 펫의 지금 경험치 순)
 *
 * @param regionCode       시도 코드 (전국이면 null)
 * @param code             시군구 코드 (시도·전국이면 null)
 * @param regionName       "전국", 시도 이름, 또는 "시도 시군구"
 * @param participantCount 순위에 든 펫 수
 * @param mine             내 펫 중 가장 높은 순위 (없으면 null)
 */
public record FatRanking(String regionCode, String code, String regionName, int participantCount,
                         List<Entry> entries, Entry mine) {

    public record Entry(int rank, String nickname, String sidoName, String sigunguName, int level, long exp, boolean mine) {
    }
}
