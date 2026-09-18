package com.bosshi.maeul.openapi.response;

import com.bosshi.maeul.openapi.entity.LDongRegion;

import java.util.List;
import java.util.stream.Collectors;

public record RegionResponse(
        String code,
        String name,
        List<SigunguResponse> sigungus
) {
    public static RegionResponse from(LDongRegion region) {
        return new RegionResponse(
                region.getCode(),
                region.getName(),
                region.getSigungus().stream()
                        .map(SigunguResponse::from)
                        .collect(Collectors.toList())
        );
    }
}

