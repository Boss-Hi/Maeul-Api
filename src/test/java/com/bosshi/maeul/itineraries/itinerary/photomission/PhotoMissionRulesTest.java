package com.bosshi.maeul.itineraries.itinerary.photomission;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("사진 미션 규칙")
class PhotoMissionRulesTest {

    @Test
    @DisplayName("거리: 위도 0.001도는 약 111m 이고 같은 점은 0이다")
    void distance() {
        assertThat(PhotoMissionRules.distanceMeters(37.2081, 126.9892, 37.2081, 126.9892)).isZero();
        assertThat(PhotoMissionRules.distanceMeters(37.2081, 126.9892, 37.2091, 126.9892)).isBetween(105.0, 117.0);
    }

    @Test
    @DisplayName("일정 기간은 시작일과 종료일을 포함한다")
    void period() {
        LocalDate start = LocalDate.of(2026, 10, 17);
        LocalDate end = LocalDate.of(2026, 10, 18);
        assertThat(PhotoMissionRules.withinPeriod(start, end, start)).isTrue();
        assertThat(PhotoMissionRules.withinPeriod(start, end, end)).isTrue();
        assertThat(PhotoMissionRules.withinPeriod(start, end, start.minusDays(1))).isFalse();
        assertThat(PhotoMissionRules.withinPeriod(start, end, end.plusDays(1))).isFalse();
    }

    @Test
    @DisplayName("촬영 시각이 30분을 넘게 차이 나면 오래된 사진이다")
    void stale() {
        LocalDateTime now = LocalDateTime.of(2026, 10, 17, 12, 0);
        assertThat(PhotoMissionRules.isStale(now.minusMinutes(30), now, Duration.ofMinutes(30))).isFalse();
        assertThat(PhotoMissionRules.isStale(now.minusMinutes(31), now, Duration.ofMinutes(30))).isTrue();
        assertThat(PhotoMissionRules.isStale(now.plusMinutes(31), now, Duration.ofMinutes(30))).isTrue();
    }

    @Test
    @DisplayName("파일 앞부분으로 JPEG, PNG, WebP 만 인정한다")
    void mime() {
        assertThat(PhotoMissionRules.detectMime(new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0, 0, 0, 0, 0, 0, 0, 0, 0})).isEqualTo("image/jpeg");
        assertThat(PhotoMissionRules.detectMime(new byte[]{(byte) 0x89, 'P', 'N', 'G', 0, 0, 0, 0, 0, 0, 0, 0})).isEqualTo("image/png");
        assertThat(PhotoMissionRules.detectMime(new byte[]{'R', 'I', 'F', 'F', 0, 0, 0, 0, 'W', 'E', 'B', 'P'})).isEqualTo("image/webp");
        assertThat(PhotoMissionRules.detectMime("GIF89a______".getBytes())).isNull();
        assertThat(PhotoMissionRules.detectMime(new byte[3])).isNull();
    }
}
