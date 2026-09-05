package com.bosshi.maeul.kakao.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "kakao.rest-api")
public class KakaoRestApiConfig {
    private String key;
    private String baseUrl;
    private String callbackUrl;
    private Integer timeoutSeconds;
    private Integer retryCount;
}
