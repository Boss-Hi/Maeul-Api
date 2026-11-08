package com.bosshi.maeul.itineraries.ai.service;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.core.JsonValue;
import com.anthropic.errors.PermissionDeniedException;
import com.anthropic.errors.UnauthorizedException;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.OutputConfig;
import com.anthropic.models.messages.StopReason;
import com.anthropic.models.messages.StructuredMessage;
import com.anthropic.models.messages.StructuredMessageCreateParams;
import com.anthropic.models.messages.StructuredOutputConfig;
import com.bosshi.maeul.itineraries.ai.config.ClaudeConfig;
import com.bosshi.maeul.itineraries.ai.type.AiProviderType;
import com.bosshi.maeul.common.exception.ServiceUnavailableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.NamedInterface;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Anthropic Claude 호출 서비스 (공식 Java SDK).
 * <ul>
 *   <li>DB 의 CLAUDE 활성 키를 돌려 쓰며, 401/403 으로 거부된 키는 비활성화 후 다음 키로 재시도</li>
 *   <li>429·5xx·overloaded 는 SDK 가 자동 재시도(기본 2회)하고, 그래도 실패하면 예외를 그대로 던짐</li>
 *   <li>structured outputs 로 응답을 지정한 타입의 JSON 스키마에 맞춰 받음</li>
 * </ul>
 */
@NamedInterface
@Slf4j
@Service
@RequiredArgsConstructor
public class ClaudeService {
    static final String REFUSAL_FALLBACK_BETA = "server-side-fallback-2026-07-01";

    private final ClaudeConfig config;
    private final AiApiKeyProvider keyProvider;

    // API 키별 클라이언트 재사용 (OkHttp 커넥션 풀 공유)
    private final Map<String, AnthropicClient> clients = new ConcurrentHashMap<>();

    // 테스트에서 교체 가능
    Function<String, AnthropicClient> clientFactory = apiKey -> AnthropicOkHttpClient.builder().apiKey(apiKey).build();

    public boolean hasAvailableApiKey() {
        return keyProvider.hasAvailableKey(AiProviderType.CLAUDE);
    }

    /**
     * 프롬프트를 보내고 응답을 {@code type} 으로 받는다.
     *
     * @throws ServiceUnavailableException 사용 가능한 키가 없을 때
     * @throws ClaudeRefusalException      안전 분류기가 요청을 거절했을 때
     */
    public <T> T generate(String prompt, Class<T> type) {
        if (!StringUtils.hasText(prompt)) {
            throw new IllegalArgumentException("Prompt must not be blank.");
        }
        StructuredMessageCreateParams<T> params = buildParams(prompt, type);

        Set<Long> triedKeyIds = new HashSet<>();
        while (true) {
            AiApiKeyProvider.UsableKey key = keyProvider.acquire(AiProviderType.CLAUDE)
                    .filter(k -> triedKeyIds.add(k.id()))
                    .orElseThrow(() -> {
                        log.error("사용 가능한 Claude API key 가 없습니다.");
                        return new ServiceUnavailableException(GeminiService.UNAVAILABLE_MESSAGE);
                    });

            try {
                StructuredMessage<T> response = client(key.value()).messages().create(params);
                return extract(response, type);
            } catch (UnauthorizedException e) {
                evict(key);
                keyProvider.deactivate(key.id(), "authentication_error");
            } catch (PermissionDeniedException e) {
                evict(key);
                keyProvider.deactivate(key.id(), "permission_error");
            }
        }
    }

    <T> StructuredMessageCreateParams<T> buildParams(String prompt, Class<T> type) {
        StructuredMessageCreateParams.Builder<T> builder = MessageCreateParams.builder()
                .model(config.getModel())
                .maxTokens(config.getMaxTokens())
                .outputConfig(StructuredOutputConfig.<T>builder()
                        .format(type)
                        .effort(OutputConfig.Effort.of(config.getEffort()))
                        .build())
                .addUserMessage(prompt);

        if (config.isRefusalFallbackEnabled()) {
            builder.putAdditionalHeader("anthropic-beta", REFUSAL_FALLBACK_BETA)
                    .putAdditionalBodyProperty("fallbacks", JsonValue.from("default"));
        }
        return builder.build();
    }

    private <T> T extract(StructuredMessage<T> response, Class<T> type) {
        StopReason stopReason = response.stopReason().orElse(null);
        if (StopReason.REFUSAL.equals(stopReason)) {
            throw new ClaudeRefusalException("Claude 가 요청을 거절했습니다.");
        }
        if (StopReason.MAX_TOKENS.equals(stopReason)) {
            throw new IllegalStateException("Claude 응답이 max_tokens 에 도달해 잘렸습니다. ai.claude.max-tokens 를 늘려주세요.");
        }
        return response.content().stream()
                .flatMap(block -> block.text().stream())
                .map(textBlock -> textBlock.text())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Claude 응답에 " + type.getSimpleName() + " 결과가 없습니다."));
    }

    private AnthropicClient client(String apiKey) {
        return clients.computeIfAbsent(apiKey, clientFactory);
    }

    private void evict(AiApiKeyProvider.UsableKey key) {
        AnthropicClient removed = clients.remove(key.value());
        if (removed != null) {
            removed.close();
        }
    }

    /**
     * Claude 안전 분류기 거절. 다음 AI 제공자로 넘어가도록 호출 측에서 처리한다.
     */
    public static class ClaudeRefusalException extends RuntimeException {
        public ClaudeRefusalException(String message) {
            super(message);
        }
    }
}
