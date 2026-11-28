package com.bosshi.maeul.itineraries.itinerary.photomission;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** 사진 미션의 순수 규칙: 거리, 기간, 촬영 시각, 이미지 형식 */
final class PhotoMissionRules {

    static final String UNAVAILABLE_MESSAGE = "사진을 확인할 수 없습니다. 잠시 후 다시 시도해주세요.";

    private static final double EARTH_RADIUS_M = 6_371_000;

    private PhotoMissionRules() {
    }

    /** 두 좌표 사이 거리(미터). Haversine */
    static double distanceMeters(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return EARTH_RADIUS_M * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    /** 일정 기간(시작일 0시 ~ 종료일 24시) 안인가 */
    static boolean withinPeriod(LocalDate startDate, LocalDate endDate, LocalDate today) {
        return !today.isBefore(startDate) && !today.isAfter(endDate);
    }

    /** 촬영 시각이 업로드 시각과 max 보다 멀면 오래된 사진이다 */
    static boolean isStale(LocalDateTime takenAt, LocalDateTime now, Duration max) {
        return Duration.between(takenAt, now).abs().compareTo(max) > 0;
    }

    /** 파일 앞부분으로 JPEG, PNG, WebP 를 판별한다. 아니면 null */
    static String detectMime(byte[] data) {
        if (data == null || data.length < 12) {
            return null;
        }
        if ((data[0] & 0xFF) == 0xFF && (data[1] & 0xFF) == 0xD8 && (data[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }
        if ((data[0] & 0xFF) == 0x89 && data[1] == 'P' && data[2] == 'N' && data[3] == 'G') {
            return "image/png";
        }
        if (data[0] == 'R' && data[1] == 'I' && data[2] == 'F' && data[3] == 'F'
                && data[8] == 'W' && data[9] == 'E' && data[10] == 'B' && data[11] == 'P') {
            return "image/webp";
        }
        return null;
    }
}
