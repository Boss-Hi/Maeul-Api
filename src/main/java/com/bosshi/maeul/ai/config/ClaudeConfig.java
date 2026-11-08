package com.bosshi.maeul.ai.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "ai.claude")
public class ClaudeConfig {
    /** 최초 기동 시 ai_api_keys 에 CLAUDE 키가 없으면 seed 되는 키. 런타임에는 DB 의 활성 키만 사용 */
    private String apiKey;
    private String model = "claude-opus-5-5";
    private long maxTokens = 16000;
    /** low, medium, high, xhigh, max */
    private String effort = "medium";
    /** 안전 분류기 거절(refusal) 시 서버 측에서 다른 Claude 모델로 재처리 */
    private boolean refusalFallbackEnabled = true;
}
