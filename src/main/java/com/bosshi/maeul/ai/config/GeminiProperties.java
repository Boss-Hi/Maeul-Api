package com.bosshi.maeul.ai.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "ai.gemini")
public class GeminiProperties {
    private String baseUrl = "https://generativelanguage.googleapis.com";
    private String apiKey;
    private String model = "gemini-1.5-flash";
    private Double temperature = 0.7;
    private Integer maxOutputTokens = 512;
    private boolean batchEnabled = false;
    private String batchPrompt = "Hello from Maeul batch.";
}

