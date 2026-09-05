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

    /**
     * Gemini API가 사용 가능한지 확인
     */
    public boolean isConfigured() {
        return apiKey != null && !apiKey.isEmpty();
    }
}