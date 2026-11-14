package com.bosshi.maeul.territory.dto;

import java.util.List;

/**
 * 내 병아리 현황
 *
 * @param chicks           키우는 중인 병아리 (분양 순)
 * @param adoptableRegions 지금 분양받을 수 있는 시군구
 */
public record ChickOverview(
        List<ChickStatus> chicks,
        List<AdoptableRegion> adoptableRegions
) {
    public record AdoptableRegion(String regionId, String regionName) {
    }
}
