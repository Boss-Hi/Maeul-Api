package com.bosshi.maeul.timeline.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.ZoneId;

/**
 * 타임라인(위치 기록) 설정
 * <p>
 * 수집 주기 등은 환경 변수로 바꿀 수 있으며, 앱은 GET /api/timeline/settings 로 현재 값을 받아 간다.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "timeline")
public class TimelineProperties {
    /** 위치 수집 주기. 같은 사용자의 위치 기록은 이 간격보다 촘촘하게 저장하지 않는다. */
    private Duration collectionInterval = Duration.ofMinutes(1);

    /** 기기 타이머 오차 허용치. 수집 주기에서 이 값만큼 짧은 간격까지는 허용한다. */
    private Duration collectionTolerance = Duration.ofSeconds(5);

    /** 이 반경 안에 머물면 같은 장소로 본다. */
    private double stayRadiusMeters = 100;

    /** 이 시간 이상 같은 장소에 있으면 머문 장소(STAY)로 본다. */
    private Duration minStayDuration = Duration.ofMinutes(5);

    /** 날짜 경계와 응답 시각에 쓰는 시간대 */
    private String zone = "Asia/Seoul";

    /** 저장되는 위치 기록 사이의 최소 간격 */
    public Duration minimumGap() {
        Duration gap = collectionInterval.minus(collectionTolerance);
        return gap.isNegative() ? Duration.ZERO : gap;
    }

    public ZoneId zoneId() {
        return ZoneId.of(zone);
    }
}
