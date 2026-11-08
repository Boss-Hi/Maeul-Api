package com.bosshi.maeul.ai.provider;

import com.bosshi.maeul.ai.AiRequest;
import com.anthropic.errors.AnthropicIoException;
import com.anthropic.errors.InternalServerException;
import com.anthropic.errors.RateLimitException;
import com.bosshi.maeul.ai.service.ClaudeService;
import com.bosshi.maeul.common.exception.ServiceUnavailableException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Anthropic Claude. structured outputs 로 응답 타입의 스키마를 강제하므로 JSON 예시는 쓰지 않는다.
 */
@Component
@RequiredArgsConstructor
public class ClaudeAiProvider implements AiProvider {
    private final ClaudeService claudeService;

    @Override
    public String name() {
        return "claude";
    }

    @Override
    public boolean isAvailable() {
        return claudeService.hasAvailableApiKey();
    }

    @Override
    public <T> T generate(AiRequest<T> request) {
        try {
            return claudeService.generate(request.prompt(), request.responseType());
        } catch (RateLimitException | InternalServerException e) {
            // SDK 재시도(429, 5xx)까지 소진. 제공자 순서와 무관하게 Gemini 과 같은 503 으로 알린다
            throw new ServiceUnavailableException(ServiceUnavailableException.OVERLOADED_MESSAGE, e);
        } catch (AnthropicIoException e) {
            throw new ServiceUnavailableException(ServiceUnavailableException.UNAVAILABLE_MESSAGE, e);
        }
    }
}
