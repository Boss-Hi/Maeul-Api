package com.bosshi.maeul.territory.response;

import com.bosshi.maeul.territory.dto.FatOverview;

import java.util.List;

/**
 * 내 펫 현황
 *
 * @param fats                     키우는 중인 펫 (분양 순)
 * @param maxActiveFats            동시에 키울 수 있는 펫 수 (fats 가 이만큼이면 분양받을 수 없다)
 * @param suggestedAdoptionRegions 추천 분양 지역: 오늘 머물렀거나 오늘 졸업한, 펫 없는 시군구 ('펫 분양받기' 버튼을 먼저 보여준다).
 *                                 분양은 전국 어느 시군구에서나 할 수 있다 (목록: GET /api/fats/regions)
 */
public record FatOverviewResponse(
        List<FatResponse> fats,
        int maxActiveFats,
        List<FatRegionResponse> suggestedAdoptionRegions
) {
    public static FatOverviewResponse from(FatOverview overview) {
        return new FatOverviewResponse(overview.fats().stream().map(FatResponse::from).toList(), overview.maxActiveFats(),
                overview.suggestedAdoptionRegions().stream().map(FatRegionResponse::from).toList());
    }
}
