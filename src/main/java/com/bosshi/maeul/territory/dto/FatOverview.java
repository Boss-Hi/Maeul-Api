package com.bosshi.maeul.territory.dto;

import java.util.List;

/**
 * 내 펫 현황
 *
 * @param fats                     키우는 중인 펫 (분양 순)
 * @param maxActiveFats            동시에 키울 수 있는 펫 수
 * @param suggestedAdoptionRegions 추천 분양 지역 (분양 버튼을 먼저 보여줄 시군구. 분양은 이곳 밖에서도 할 수 있다)
 */
public record FatOverview(
        List<FatStatus> fats,
        int maxActiveFats,
        List<FatRegion> suggestedAdoptionRegions
) {
}
