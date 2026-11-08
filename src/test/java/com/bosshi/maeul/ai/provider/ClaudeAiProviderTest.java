package com.bosshi.maeul.ai.provider;

import com.anthropic.core.JsonValue;
import com.anthropic.core.http.Headers;
import com.anthropic.errors.AnthropicIoException;
import com.anthropic.errors.InternalServerException;
import com.anthropic.errors.RateLimitException;
import com.bosshi.maeul.ai.AiRequest;
import com.bosshi.maeul.ai.service.ClaudeService;
import com.bosshi.maeul.common.exception.ServiceUnavailableException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("ClaudeAiProvider 테스트")
class ClaudeAiProviderTest {

    record Answer(String value) {
    }

    private final ClaudeService claudeService = mock(ClaudeService.class);
    private final ClaudeAiProvider provider = new ClaudeAiProvider(claudeService);
    private final AiRequest<Answer> request = AiRequest.of("prompt", Answer.class, null);

    @Test
    @DisplayName("성공하면 서비스가 만든 값을 그대로 돌려준다")
    void returnsServiceResult() {
        when(claudeService.generate(anyString(), eq(Answer.class))).thenReturn(new Answer("ok"));

        assertThat(provider.generate(request)).isEqualTo(new Answer("ok"));
    }

    @Test
    @DisplayName("SDK 재시도를 소진한 429/5xx 는 과부하 메시지의 503 으로 바꾼다")
    void mapsRateLimitAndServerErrorToServiceUnavailable() {
        doThrow(RateLimitException.builder().headers(Headers.builder().build()).body(JsonValue.from(Map.of())).build())
                .when(claudeService).generate(anyString(), eq(Answer.class));
        assertThatThrownBy(() -> provider.generate(request))
                .isInstanceOf(ServiceUnavailableException.class)
                .hasMessage(ServiceUnavailableException.OVERLOADED_MESSAGE);

        doThrow(InternalServerException.builder().statusCode(529).headers(Headers.builder().build()).body(JsonValue.from(Map.of())).build())
                .when(claudeService).generate(anyString(), eq(Answer.class));
        assertThatThrownBy(() -> provider.generate(request))
                .isInstanceOf(ServiceUnavailableException.class)
                .hasMessage(ServiceUnavailableException.OVERLOADED_MESSAGE);
    }

    @Test
    @DisplayName("연결 실패는 사용 불가 메시지의 503 으로 바꾼다")
    void mapsIoFailureToServiceUnavailable() {
        doThrow(new AnthropicIoException("connect timed out")).when(claudeService).generate(anyString(), eq(Answer.class));

        assertThatThrownBy(() -> provider.generate(request))
                .isInstanceOf(ServiceUnavailableException.class)
                .hasMessage(ServiceUnavailableException.UNAVAILABLE_MESSAGE);
    }

    @Test
    @DisplayName("거절은 과부하가 아니므로 그대로 던진다")
    void passesRefusalThrough() {
        doThrow(new ClaudeService.ClaudeRefusalException("거절")).when(claudeService).generate(anyString(), eq(Answer.class));

        assertThatThrownBy(() -> provider.generate(request)).isInstanceOf(ClaudeService.ClaudeRefusalException.class);
    }
}
