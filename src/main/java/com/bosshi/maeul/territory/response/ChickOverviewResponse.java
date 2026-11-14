package com.bosshi.maeul.territory.response;

import com.bosshi.maeul.territory.dto.ChickOverview;
import com.bosshi.maeul.territory.dto.ChickOverview.AdoptableRegion;

import java.util.List;

/**
 * 내 병아리 현황
 *
 * @param chicks           키우는 중인 병아리 (분양 순)
 * @param adoptableRegions 지금 분양받을 수 있는 시군구 (비어 있지 않으면 '병아리 분양받기' 버튼을 보여준다)
 */
public record ChickOverviewResponse(
        List<ChickResponse> chicks,
        List<AdoptableRegion> adoptableRegions
) {
    public static ChickOverviewResponse from(ChickOverview overview) {
        return new ChickOverviewResponse(overview.chicks().stream().map(ChickResponse::from).toList(), overview.adoptableRegions());
    }
}
