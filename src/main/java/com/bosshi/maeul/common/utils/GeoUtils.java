package com.bosshi.maeul.common.utils;

import org.springframework.modulith.NamedInterface;

/**
 * 지리 정보 처리 관련 유틸리티 클래스
 */
@NamedInterface
public class GeoUtils {

    private static final int EARTH_RADIUS_KM = 6371; // 지구의 반지름 (km)

    /**
     * Haversine 공식을 사용하여 두 좌표 사이의 거리를 계산합니다.
     *
     * @param lat1 첫 번째 위도
     * @param lon1 첫 번째 경도
     * @param lat2 두 번째 위도
     * @param lon2 두 번째 경도
     * @return 거리 (km)
     */
    public static Double calculateDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
            return Double.MAX_VALUE;
        }

        Double dLat = Math.toRadians(lat2 - lat1);
        Double dLon = Math.toRadians(lon2 - lon1);

        Double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2);

        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }

    /**
     * 두 좌표가 지정된 반경 내에 있는지 확인합니다.
     *
     * @param lat1 기준 위도
     * @param lon1 기준 경도
     * @param lat2 비교 위도
     * @param lon2 비교 경도
     * @param radiusKm 반경 (km)
     * @return 반경 내에 있으면 true
     */
    public static Boolean isWithinRadius(Double lat1, Double lon1, Double lat2, Double lon2, Integer radiusKm) {
        Double distance = calculateDistance(lat1, lon1, lat2, lon2);
        return distance <= radiusKm;
    }
}

