package com.bosshi.maeul.ai.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "ai.gemini")
public class GeminiConfig {
    private String baseUrl;
    private String apiKey;
    private String model;
    private Double temperature;
    private Integer maxOutputTokens;
    private boolean batchEnabled;
    private String batchPrompt;

    /** 일시적 오류(500/503/504) 시 모델당 최대 시도 횟수 */
    private int retryMaxAttempts = 3;
    /** 첫 재시도 대기 시간(ms). 이후 2배씩 증가 */
    private long retryInitialBackoffMs = 1000;
    /** 기본 모델 재시도가 모두 실패했을 때 사용할 대체 모델 (비어 있으면 사용 안 함) */
    private String fallbackModel;

    /**
     * Gemini API가 사용 가능한지 확인
     */
    public boolean isConfigured() {
        return apiKey != null && !apiKey.isEmpty();
    }
}