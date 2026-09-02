package com.bosshi.maeul.ai.service;

import com.bosshi.maeul.ai.config.GeminiConfig;
import com.bosshi.maeul.ai.entity.GeminiApiKey;
import com.bosshi.maeul.ai.repository.GeminiApiKeyRepository;
import com.bosshi.maeul.ai.request.GeminiGenerateRequest;
import com.bosshi.maeul.ai.response.GeminiGenerateResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.NamedInterface;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@NamedInterface
@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiService {
    private final RestClient.Builder restClientBuilder;
    private final GeminiConfig config;
    private final GeminiApiKeyRepository geminiApiKeyRepository;

    public GeminiGenerateResponse generateText(String prompt) {
        if (!StringUtils.hasText(prompt)) {
            throw new IllegalArgumentException("Prompt must not be blank.");
        }

        return generate(prompt);
    }

    @Transactional
    public GeminiGenerateResponse generate(String prompt) {
        String apiKey = getApiKey();
        validateApiKey(apiKey);

        GeminiGenerateRequest request = buildPrompt(
                prompt,
                config.getTemperature(),
                config.getMaxOutputTokens()
        );
        URI uri = buildGenerateUri(apiKey);

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

    @Transactional
    public String getApiKey() {
        Optional<GeminiApiKey> keyOptional = geminiApiKeyRepository.findNextApiKey();
        if (keyOptional.isPresent()) {
            GeminiApiKey key = keyOptional.get();
            key.setLastUsedAt(java.time.LocalDateTime.now());
            geminiApiKeyRepository.save(key);
            log.info("Gemini API key from database used: id={}", key.getId());
            return key.getApiKey();
        }
        log.info("Gemini API key from configuration used.");
        return config.getApiKey();
    }

    private URI buildGenerateUri(String apiKey) {
        String baseUrl = config.getBaseUrl();
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }

        return UriComponentsBuilder
                .fromUriString(baseUrl)
                .path("/v1/models/{model}:generateContent")
                .queryParam("key", apiKey)
                .buildAndExpand(config.getModel())
                .toUri();
    }

    private void validateApiKey(String apiKey) {
        if (!StringUtils.hasText(apiKey)) {
            throw new IllegalStateException("GEMINI_API_KEY is required.");
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

