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
    private String baseUrl = "https://generativelanguage.googleapis.com";
    private String apiKey;
    private String model = "gemini-3.5-flash";
    private Double temperature = 0.5;
    private Integer maxOutputTokens = 4096;
    private boolean batchEnabled = false;
    private String batchPrompt = "Hello from Maeul batch.";

    /**
     * Gemini API가 사용 가능한지 확인
     */
    public boolean isConfigured() {
        return apiKey != null && !apiKey.isEmpty();
    }
}