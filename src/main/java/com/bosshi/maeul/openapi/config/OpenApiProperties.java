package com.bosshi.maeul.openapi.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "open-api")
public class OpenApiProperties {
    private String baseUrl;
    private String serviceKey;
    private String mobileOs;
    private String mobileApp;
    private String responseType;
}
