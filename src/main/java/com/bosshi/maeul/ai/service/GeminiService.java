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
import java.util.List;

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

        return generate(prompt);
    }

    public GeminiGenerateResponse generate(String prompt) {
        validateApiKey();

        GeminiGenerateRequest request = buildPrompt(
                prompt,
                config.getTemperature(),
                config.getMaxOutputTokens()
        );
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
            log.error("Gemini API 호출 실패 - HTTP Status: {}, Request URI: {}, Response Body: {}", e.getStatusCode(), uri, e.getResponseBodyAsString());
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
                .path("/v1/models/{model}:generateContent")
                .queryParam("key", config.getApiKey())
                .buildAndExpand(config.getModel())
                .toUri();
    }

    private void validateApiKey() {
        if (!StringUtils.hasText(config.getApiKey())) {
            throw new IllegalStateException("GEMINI_API_KEY environment variable is required.");
        }
    }

    public GeminiGenerateRequest buildPrompt(String prompt, Double temperature, Integer maxOutputTokens) {
        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException("Can not create GeminiGenerateRequest with null or blank prompt");
        }

        GeminiGenerateRequest.Part part = new GeminiGenerateRequest.Part(prompt);
        GeminiGenerateRequest.Content content = new GeminiGenerateRequest.Content("user", List.of(part));
        GeminiGenerateRequest.GenerationConfig config = new GeminiGenerateRequest.GenerationConfig(temperature, maxOutputTokens, "application/json");
        return new GeminiGenerateRequest(List.of(content), config);
    }
}

