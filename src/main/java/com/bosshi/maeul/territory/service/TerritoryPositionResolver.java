package com.bosshi.maeul.territory.service;

import com.bosshi.maeul.territory.entity.TerritoryRegionCell;
import com.bosshi.maeul.timeline.entity.TimelinePoint;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 위치 기록마다 시군구를 붙인다.
 */
@Component
@RequiredArgsConstructor
public class TerritoryPositionResolver {

    private final TerritoryRegionLocator locator;

    public List<OccupationCalculator.RegionPoint> resolve(List<TimelinePoint> points) {
        List<String> keys = points.stream()
                .map(point -> TerritoryRegionLocator.cellKey(point.getLatitude(), point.getLongitude()))
                .toList();
        Map<String, TerritoryRegionCell> cells = locator.locate(keys);
        return java.util.stream.IntStream.range(0, points.size())
                .mapToObj(i -> {
                    TerritoryRegionCell cell = cells.get(keys.get(i));
                    return new OccupationCalculator.RegionPoint(points.get(i).getRecordedAt(), cell == null ? null : cell.getRegionId());
                })
                .toList();
    }
}
