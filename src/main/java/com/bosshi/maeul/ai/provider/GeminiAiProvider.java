package com.bosshi.maeul.ai.provider;

import com.bosshi.maeul.ai.AiRequest;
import com.bosshi.maeul.ai.response.GeminiGenerateResponse;
import com.bosshi.maeul.ai.service.GeminiService;
import com.bosshi.maeul.common.exception.ServiceUnavailableException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

/**
 * Google Gemini. 스키마를 강제할 수 없어서 프롬프트로 JSON 출력을 요구하고 응답 텍스트를 응답 타입으로 파싱한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GeminiAiProvider implements AiProvider {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final GeminiService geminiService;

    @Override
    public String name() {
        return "gemini";
    }

    @Override
    public boolean isAvailable() {
        return geminiService.hasAvailableApiKey();
    }

    @Override
    public <T> T generate(AiRequest<T> request) {
        GeminiGenerateResponse response = callGemini(request);
        if (response != null && "MAX_TOKENS".equals(response.getFirstFinishReason())) {
            throw new IllegalStateException("Gemini 응답이 maxOutputTokens 에 도달해 잘렸습니다. ai.gemini.max-output-tokens 를 늘려주세요.");
        }
        String result = response != null ? response.getFirstText() : null;
        log.info("Gemini API 호출 성공: {}", result);
        if (result == null || result.isBlank()) {
            throw new IllegalStateException("Gemini API 응답 결과가 비어있습니다.");
        }
        try {
            return OBJECT_MAPPER.readValue(cleanMarkdownJson(result), request.responseType());
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Gemini 응답을 " + request.responseType().getSimpleName() + " 로 파싱하지 못했습니다.", e);
        }
    }

    /**
     * 요청 제한(429)과 연결 실패는 제공자 순서와 무관하게 같은 503 으로 알린다.
     * 일시적 서버 오류의 재시도 소진은 GeminiService 가 이미 503 으로 던진다.
     */
    private GeminiGenerateResponse callGemini(AiRequest<?> request) {
        try {
            return geminiService.generate(withOutputInstruction(request));
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == HttpStatus.TOO_MANY_REQUESTS.value()) {
                throw new ServiceUnavailableException(ServiceUnavailableException.OVERLOADED_MESSAGE, e);
            }
            throw e;
        } catch (ResourceAccessException e) {
            throw new ServiceUnavailableException(ServiceUnavailableException.UNAVAILABLE_MESSAGE, e);
        }
    }

    private static String withOutputInstruction(AiRequest<?> request) {
        StringBuilder prompt = new StringBuilder(request.prompt());
        prompt.append("\n출력은 설명, 인사말, 마크다운(```json) 없이 오직 JSON 데이터만 반환하세요.\n");
        if (request.jsonExample() != null) {
            prompt.append("[응답 JSON 형식]\n").append(request.jsonExample()).append("\n");
        }
        return prompt.toString();
    }

    static String cleanMarkdownJson(String raw) {
        if (raw == null) return null;
        raw = raw.trim();
        if (raw.startsWith("```json")) {
            raw = raw.substring(7);
        } else if (raw.startsWith("```")) {
            raw = raw.substring(3);
        }
        if (raw.endsWith("```")) {
            raw = raw.substring(0, raw.length() - 3);
        }
        return raw.trim();
    }
}
