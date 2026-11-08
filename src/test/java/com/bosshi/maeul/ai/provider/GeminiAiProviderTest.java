package com.bosshi.maeul.ai.provider;

import com.bosshi.maeul.ai.AiRequest;
import com.bosshi.maeul.ai.response.GeminiGenerateResponse;
import com.bosshi.maeul.ai.service.GeminiService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("GeminiAiProvider 테스트")
class GeminiAiProviderTest {

    record Answer(String value) {
    }

    private final GeminiService geminiService = mock(GeminiService.class);
    private final GeminiAiProvider provider = new GeminiAiProvider(geminiService);

    @Test
    @DisplayName("마크다운 펜스가 붙은 JSON 응답을 응답 타입으로 파싱한다")
    void parsesFencedJson() {
        reply("```json\n{\"value\":\"ok\"}\n```");

        Answer answer = provider.generate(AiRequest.of("prompt", Answer.class, null));

        assertThat(answer.value()).isEqualTo("ok");
    }

    @Test
    @DisplayName("프롬프트 뒤에 JSON 출력 지시와 응답 예시를 붙여 보낸다")
    void appendsOutputInstructionAndExample() {
        reply("{\"value\":\"ok\"}");

        provider.generate(AiRequest.of("원래 프롬프트", Answer.class, "{\"value\":\"예시\"}"));

        ArgumentCaptor<String> sent = ArgumentCaptor.forClass(String.class);
        verify(geminiService).generate(sent.capture());
        assertThat(sent.getValue()).startsWith("원래 프롬프트").contains("{\"value\":\"예시\"}");
    }

    @Test
    @DisplayName("JSON 이 아니거나 비어 있으면 실패한다 (다음 제공자로 넘어가도록)")
    void failsOnUnparsableOrEmpty() {
        reply("죄송합니다, 일정을 만들 수 없습니다.");
        assertThatThrownBy(() -> provider.generate(AiRequest.of("p", Answer.class, null)))
                .isInstanceOf(IllegalStateException.class);

        reply(" ");
        assertThatThrownBy(() -> provider.generate(AiRequest.of("p", Answer.class, null)))
                .isInstanceOf(IllegalStateException.class);
    }

    private void reply(String text) {
        GeminiGenerateResponse response = mock(GeminiGenerateResponse.class);
        when(response.getFirstText()).thenReturn(text);
        when(geminiService.generate(anyString())).thenReturn(response);
    }
}
