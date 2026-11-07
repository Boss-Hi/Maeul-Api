package com.bosshi.maeul.territory.service;

import com.bosshi.maeul.kakao.KakaoClient;
import com.bosshi.maeul.kakao.response.KakaoRegionResponse;
import com.bosshi.maeul.territory.entity.TerritoryRegionCell;
import com.bosshi.maeul.territory.repository.TerritoryRegionCellRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 좌표가 속한 시군구를 찾는다.
 * <p>
 * 좌표를 소수점 3자리 격자(약 100m)로 묶어 카카오 역지오코딩 결과를 DB 에 캐시한다.
 * 카카오 호출이 실패한 격자는 캐시하지 않고 결과에서 빠지며, 다음 재계산 때 다시 시도한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TerritoryRegionLocator {

    private static final String REGION_CODE_ENDPOINT = "/v2/local/geo/coord2regioncode.json";

    private final TerritoryRegionCellRepository cellRepository;
    private final KakaoClient kakaoClient;

    public static String cellKey(double latitude, double longitude) {
        return String.format(Locale.ROOT, "%.3f:%.3f", latitude, longitude);
    }

    /**
     * @return 격자 키 -> 격자 정보. 판정하지 못한 격자는 없다.
     */
    public Map<String, TerritoryRegionCell> locate(Collection<String> cellKeys) {
        Set<String> keys = new LinkedHashSet<>(cellKeys);
        Map<String, TerritoryRegionCell> cells = new HashMap<>();
        cellRepository.findAllById(keys).forEach(cell -> cells.put(cell.getCellKey(), cell));

        List<TerritoryRegionCell> resolved = new ArrayList<>();
        for (String key : keys) {
            if (cells.containsKey(key)) {
                continue;
            }
            try {
                TerritoryRegionCell cell = resolve(key);
                cells.put(key, cell);
                resolved.add(cell);
            } catch (RuntimeException e) {
                // 카카오 장애 시 같은 배치에서 계속 기다리지 않는다
                log.warn("Kakao region lookup failed for cell {}: {}", key, e.getMessage());
                break;
            }
        }
        cellRepository.saveAll(resolved);
        return cells;
    }

    private TerritoryRegionCell resolve(String key) {
        String[] parts = key.split(":");
        KakaoRegionResponse response = kakaoClient.call(REGION_CODE_ENDPOINT, Map.of("x", parts[1], "y", parts[0]));
        TerritoryRegionCell.TerritoryRegionCellBuilder cell = TerritoryRegionCell.builder().cellKey(key);
        if (response == null || response.getDocuments() == null) {
            return cell.build();
        }
        return response.getDocuments().stream()
                .filter(doc -> "B".equals(doc.getRegionType()))
                .filter(doc -> doc.getCode() != null && doc.getCode().length() >= 5)
                .filter(doc -> doc.getRegion1depthName() != null && !doc.getRegion1depthName().isBlank())
                .findFirst()
                .map(doc -> cell
                        .regionId(doc.getCode().substring(0, 5))
                        .regionName(doc.getRegion2depthName() == null || doc.getRegion2depthName().isBlank()
                                ? doc.getRegion1depthName() : doc.getRegion2depthName())
                        .parentRegionId(doc.getCode().substring(0, 2))
                        .parentName(doc.getRegion1depthName())
                        .build())
                .orElseGet(cell::build); // 바다 등 지역 밖
    }
}
