package com.bosshi.maeul.ai.service;

import com.bosshi.maeul.ai.config.GeminiConfig;
import com.bosshi.maeul.ai.request.GeminiGenerateRequest;
import com.bosshi.maeul.ai.response.GeminiGenerateResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.NamedInterface;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@NamedInterface
@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiService {
    private final RestClient.Builder restClientBuilder;
    private final GeminiConfig config;

    public GeminiGenerateResponse generateText(String prompt) {
        if (!StringUtils.hasText(prompt)) {
            throw new IllegalArgumentException("Prompt must not be blank.");
        }

        GeminiGenerateRequest request = GeminiGenerateRequest.ofPrompt(
                prompt,
                config.getTemperature(),
                config.getMaxOutputTokens()
        );

        return generate(request);
    }

    public GeminiGenerateResponse generate(GeminiGenerateRequest request) {
        validateApiKey();

        URI uri = buildGenerateUri();

        try {
            return restClientBuilder
                    .build()
                    .post()
                    .uri(uri)
                    .body(request)
                    .retrieve()
                    .body(GeminiGenerateResponse.class);
        } catch (RestClientResponseException e) {
            log.error("Gemini API 호출 실패 - HTTP Status: {}, Request URI: {}, Response Body: {}",
                    e.getStatusCode(), uri, e.getResponseBodyAsString());
            throw e;
        }
    }

    private URI buildGenerateUri() {
        String baseUrl = config.getBaseUrl();
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }

        return UriComponentsBuilder
                .fromUriString(baseUrl)
                .path("/v1beta/models/{model}:generateContent")
                .queryParam("key", config.getApiKey())
                .buildAndExpand(config.getModel())
                .toUri();
    }

    private void validateApiKey() {
        if (!StringUtils.hasText(config.getApiKey())) {
            throw new IllegalStateException("GEMINI_API_KEY environment variable is required.");
        }
    }
}

