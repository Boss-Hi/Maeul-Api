package com.bosshi.maeul.territory.response;

import com.bosshi.maeul.territory.dto.FatOverview;
import com.bosshi.maeul.territory.dto.FatOverview.AdoptableRegion;

import java.util.List;

/**
 * 내 병아리 현황
 *
 * @param fats           키우는 중인 병아리 (분양 순)
 * @param adoptableRegions 지금 분양받을 수 있는 시군구 (비어 있지 않으면 '병아리 분양받기' 버튼을 보여준다)
 */
public record FatOverviewResponse(
        List<FatResponse> fats,
        List<AdoptableRegion> adoptableRegions
) {
    public static FatOverviewResponse from(FatOverview overview) {
        return new FatOverviewResponse(overview.fats().stream().map(FatResponse::from).toList(), overview.adoptableRegions());
    }
}
