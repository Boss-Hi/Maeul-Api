package com.bosshi.maeul.territory.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 점령 설정
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "territory")
public class TerritoryProperties {
    /** 연속된 두 위치 기록 사이가 이보다 길면 그 사이 시간은 점령 시간에 넣지 않는다. */
    private Duration maxGap = Duration.ofMinutes(3);

    /** 이 점령 시간마다 포인트를 준다. */
    private Duration rewardUnit = Duration.ofHours(1);

    /** rewardUnit 마다 주는 포인트 */
    private long rewardPoints = 100;

    /** 랭킹 목록 최대 크기 */
    private int rankingMaxSize = 100;
}
