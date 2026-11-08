package com.bosshi.maeul.ai.service;

import com.anthropic.client.AnthropicClient;
import com.anthropic.core.JsonValue;
import com.anthropic.core.http.Headers;
import com.anthropic.errors.UnauthorizedException;
import com.anthropic.models.messages.OutputConfig;
import com.anthropic.models.messages.StopReason;
import com.anthropic.models.messages.StructuredContentBlock;
import com.anthropic.models.messages.StructuredMessage;
import com.anthropic.models.messages.StructuredMessageCreateParams;
import com.anthropic.models.messages.StructuredTextBlock;
import com.bosshi.maeul.ai.config.ClaudeConfig;
import com.bosshi.maeul.ai.type.AiProviderType;
import com.bosshi.maeul.common.exception.ServiceUnavailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("ClaudeService 테스트")
class ClaudeServiceTest {

    record Answer(String value) {
    }

    private AiApiKeyProvider keyProvider;
    private ClaudeService service;
    private final Map<String, AnthropicClient> clientsByKey = new HashMap<>();

    @BeforeEach
    void setUp() {
        keyProvider = mock(AiApiKeyProvider.class);
        service = new ClaudeService(new ClaudeConfig(), keyProvider);
        service.clientFactory = clientsByKey::get;
    }

    @Test
    @DisplayName("요청에 모델, effort, 스키마, 거절 시 서버 측 대체(fallbacks=default)를 담는다")
    void buildsRequestWithDefaults() {
        StructuredMessageCreateParams<Answer> params = service.buildParams("prompt", Answer.class);

        assertThat(params.rawParams().model().asString()).isEqualTo("claude-opus-5-5");
        assertThat(params.rawParams().maxTokens()).isEqualTo(16000L);
        OutputConfig outputConfig = params.rawParams().outputConfig().orElseThrow();
        assertThat(outputConfig.effort()).contains(OutputConfig.Effort.MEDIUM);
        assertThat(outputConfig.format()).isPresent();
        assertThat(params.rawParams()._additionalBodyProperties()).containsEntry("fallbacks", JsonValue.from("default"));
        assertThat(params.rawParams()._headers().values("anthropic-beta")).contains(ClaudeService.REFUSAL_FALLBACK_BETA);
    }

    @Test
    @DisplayName("401 이면 키를 비활성화하고 다음 키로 재시도한다")
    void deactivatesUnauthorizedKeyAndRetries() {
        when(keyProvider.acquire(AiProviderType.CLAUDE)).thenReturn(
                Optional.of(new AiApiKeyProvider.UsableKey(1L, "A", "bad")),
                Optional.of(new AiApiKeyProvider.UsableKey(2L, "B", "good")));
        clientsByKey.put("bad", clientThrowing(UnauthorizedException.builder()
                .headers(Headers.builder().build()).body(JsonValue.from(Map.of())).build()));
        clientsByKey.put("good", clientReturning(response(StopReason.END_TURN, new Answer("ok"))));

        Answer answer = service.generate("prompt", Answer.class);

        assertThat(answer.value()).isEqualTo("ok");
        verify(keyProvider).deactivate(1L, "authentication_error");
    }

    @Test
    @DisplayName("거절(refusal)이면 ClaudeRefusalException 을 던진다")
    void throwsOnRefusal() {
        when(keyProvider.acquire(AiProviderType.CLAUDE)).thenReturn(Optional.of(new AiApiKeyProvider.UsableKey(1L, "A", "k")));
        clientsByKey.put("k", clientReturning(response(StopReason.REFUSAL, null)));

        assertThatThrownBy(() -> service.generate("prompt", Answer.class))
                .isInstanceOf(ClaudeService.ClaudeRefusalException.class);
    }

    @Test
    @DisplayName("사용 가능한 키가 없으면 503 예외를 던진다")
    void throwsWhenNoKey() {
        when(keyProvider.acquire(AiProviderType.CLAUDE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.generate("prompt", Answer.class))
                .isInstanceOf(ServiceUnavailableException.class);
    }

    @SuppressWarnings("unchecked")
    private StructuredMessage<Answer> response(StopReason stopReason, Answer answer) {
        StructuredMessage<Answer> message = mock(StructuredMessage.class);
        when(message.stopReason()).thenReturn(Optional.of(stopReason));
        if (answer != null) {
            StructuredTextBlock<Answer> text = mock(StructuredTextBlock.class);
            when(text.text()).thenReturn(answer);
            StructuredContentBlock<Answer> block = mock(StructuredContentBlock.class);
            when(block.text()).thenReturn(Optional.of(text));
            when(message.content()).thenReturn(List.of(block));
        }
        return message;
    }

    @SuppressWarnings("unchecked")
    private AnthropicClient clientReturning(StructuredMessage<Answer> message) {
        AnthropicClient client = mock(AnthropicClient.class, RETURNS_DEEP_STUBS);
        when(client.messages().create(any(StructuredMessageCreateParams.class))).thenReturn(message);
        return client;
    }

    @SuppressWarnings("unchecked")
    private AnthropicClient clientThrowing(RuntimeException e) {
        AnthropicClient client = mock(AnthropicClient.class, RETURNS_DEEP_STUBS);
        when(client.messages().create(any(StructuredMessageCreateParams.class))).thenThrow(e);
        return client;
    }
}
