package com.bosshi.maeul.ai.service;

import com.bosshi.maeul.ai.config.GeminiConfig;
import com.bosshi.maeul.ai.request.GeminiGenerateRequest;
import com.bosshi.maeul.ai.response.GeminiGenerateResponse;
import com.bosshi.maeul.common.exception.ServiceUnavailableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.NamedInterface;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@NamedInterface
@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiService {
    static final String UNAVAILABLE_MESSAGE = "일정 생성 서비스를 일시적으로 사용할 수 없습니다. 잠시 후 다시 시도해주세요.";
    static final String OVERLOADED_MESSAGE = "현재 AI 사용량이 많아 일정을 생성하지 못했습니다. 잠시 후 다시 시도해주세요.";

    private final RestClient.Builder restClientBuilder;
    private final GeminiConfig config;
    private final GeminiApiKeyProvider keyProvider;

    // 테스트에서 대기 없이 검증할 수 있도록 교체 가능
    Sleeper sleeper = Thread::sleep;

    public GeminiGenerateResponse generateText(String prompt) {
        if (!StringUtils.hasText(prompt)) {
            throw new IllegalArgumentException("Prompt must not be blank.");
        }

        return generate(prompt);
    }

    /**
     * 사용 가능한(활성) API 키가 하나라도 있는지 확인한다.
     */
    public boolean hasAvailableApiKey() {
        return keyProvider.hasAvailableKey();
    }

    /**
     * Gemini 를 호출한다.
     * <ul>
     *   <li>키가 거부되면(API_KEY_INVALID, PERMISSION_DENIED) 해당 키를 비활성화하고 다음 활성 키로 재시도</li>
     *   <li>일시적 서버 오류(500, 503 high demand, 504)는 지수 백오프로 재시도하고,
     *       소진되면 대체 모델(fallback-model, 설정 시)로 같은 과정을 반복</li>
     *   <li>그 외 오류(429 등)는 재시도하지 않음</li>
     * </ul>
     *
     * @throws ServiceUnavailableException 사용 가능한 키가 없거나, 일시적 오류 재시도가 모두 실패했을 때
     */
    public GeminiGenerateResponse generate(String prompt) {
        GeminiGenerateRequest request = buildPrompt(
                prompt,
                config.getTemperature(),
                config.getMaxOutputTokens()
        );

        int maxAttempts = Math.max(1, config.getRetryMaxAttempts());
        for (String model : candidateModels()) {
            for (int attempt = 1; attempt <= maxAttempts; attempt++) {
                try {
                    return callWithAvailableKey(model, request);
                } catch (RestClientResponseException e) {
                    if (!isTransient(e)) {
                        throw e;
                    }
                    log.warn("Gemini 일시적 오류 - model={}, attempt={}/{}, status={}",
                            model, attempt, maxAttempts, e.getStatusCode());
                    if (attempt < maxAttempts) {
                        backoff(attempt);
                    }
                }
            }
            log.error("Gemini 재시도 소진 - model={}", model);
        }
        throw new ServiceUnavailableException(OVERLOADED_MESSAGE);
    }

    private GeminiGenerateResponse callWithAvailableKey(String model, GeminiGenerateRequest request) {
        Set<Long> triedKeyIds = new HashSet<>();
        while (true) {
            GeminiApiKeyProvider.UsableKey key = keyProvider.acquire()
                    .filter(k -> triedKeyIds.add(k.id()))
                    .orElseThrow(() -> {
                        log.error("사용 가능한 Gemini API key 가 없습니다.");
                        return new ServiceUnavailableException(UNAVAILABLE_MESSAGE);
                    });

            URI uri = buildGenerateUri(model, key.value());
            try {
                return restClientBuilder
                        .build()
                        .post()
                        .uri(uri)
                        .body(request)
                        .retrieve()
                        .body(GeminiGenerateResponse.class);
            } catch (RestClientResponseException e) {
                String reason = rejectedKeyReason(e);
                if (reason != null) {
                    keyProvider.deactivate(key.id(), reason);
                    continue;
                }
                if (!isTransient(e)) {
                    log.error("Gemini API 호출 실패 - HTTP Status: {}, Request URI: {}, Response Body: {}",
                            e.getStatusCode(), maskApiKey(uri), e.getResponseBodyAsString());
                }
                throw e;
            }
        }
    }

    private List<String> candidateModels() {
        List<String> models = new ArrayList<>();
        models.add(config.getModel());
        String fallback = config.getFallbackModel();
        if (StringUtils.hasText(fallback) && !fallback.equals(config.getModel())) {
            models.add(fallback);
        }
        return models;
    }

    static boolean isTransient(RestClientResponseException e) {
        int status = e.getStatusCode().value();
        return status == 500 || status == 503 || status == 504;
    }

    private void backoff(int attempt) {
        long delay = config.getRetryInitialBackoffMs() * (1L << (attempt - 1));
        try {
            sleeper.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServiceUnavailableException(OVERLOADED_MESSAGE);
        }
    }

    @FunctionalInterface
    interface Sleeper {
        void sleep(long millis) throws InterruptedException;
    }

    /**
     * 키 자체가 거부된 응답이면 사유를, 아니면 null 을 반환한다.
     */
    static String rejectedKeyReason(RestClientResponseException e) {
        String body = e.getResponseBodyAsString();
        int status = e.getStatusCode().value();
        if (status == 400 && body.contains("\"API_KEY_INVALID\"")) {
            return "API_KEY_INVALID";
        }
        if (status == 403 && body.contains("\"PERMISSION_DENIED\"")) {
            return "PERMISSION_DENIED";
        }
        return null;
    }

    static String maskApiKey(URI uri) {
        return uri.toString().replaceAll("([?&]key=)[^&]*", "$1****");
    }

    private URI buildGenerateUri(String model, String apiKey) {
        String baseUrl = config.getBaseUrl();
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }

        return UriComponentsBuilder
                .fromUriString(baseUrl)
                .path("/v1/models/{model}:generateContent")
                .queryParam("key", apiKey)
                .buildAndExpand(model)
                .toUri();
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

