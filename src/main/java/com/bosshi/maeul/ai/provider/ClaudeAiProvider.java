package com.bosshi.maeul.ai.provider;

import com.bosshi.maeul.ai.AiRequest;
import com.bosshi.maeul.ai.service.ClaudeService;
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
        return claudeService.generate(request.prompt(), request.responseType());
    }
}
