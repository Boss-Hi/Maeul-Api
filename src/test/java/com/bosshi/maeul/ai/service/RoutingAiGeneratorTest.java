package com.bosshi.maeul.ai.service;

import com.bosshi.maeul.ai.AiRequest;
import com.bosshi.maeul.ai.AiResult;
import com.bosshi.maeul.ai.provider.AiProvider;
import com.bosshi.maeul.common.exception.ServiceUnavailableException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("RoutingAiGenerator 테스트")
class RoutingAiGeneratorTest {

    private final List<String> calls = new ArrayList<>();
    private final AiRequest<String> request = AiRequest.of("prompt", String.class, null);

    @Test
    @DisplayName("앞 제공자가 실패하면 다음 제공자로 넘어간다")
    void failsOverToNextProvider() {
        RoutingAiGenerator generator = new RoutingAiGenerator(
                List.of(fake("claude", true, null), fake("gemini", true, "ok")), List.of("claude", "gemini"));

        AiResult<String> result = generator.generate(request);

        assertThat(result).isEqualTo(new AiResult<>("gemini", "ok"));
        assertThat(calls).containsExactly("claude", "gemini");
    }

    @Test
    @DisplayName("설정한 순서대로 시도한다")
    void followsConfiguredOrder() {
        RoutingAiGenerator generator = new RoutingAiGenerator(
                List.of(fake("claude", true, "c"), fake("gemini", true, "g")), List.of("gemini", "claude"));

        assertThat(generator.generate(request).provider()).isEqualTo("gemini");
        assertThat(calls).containsExactly("gemini");
    }

    @Test
    @DisplayName("키가 없는 제공자는 호출하지 않고 건너뛴다")
    void skipsUnavailableProvider() {
        RoutingAiGenerator generator = new RoutingAiGenerator(
                List.of(fake("claude", false, "c"), fake("gemini", true, "g")), List.of("claude", "gemini"));

        assertThat(generator.generate(request).provider()).isEqualTo("gemini");
        assertThat(calls).containsExactly("gemini");
    }

    @Test
    @DisplayName("제외한 제공자는 호출하지 않는다")
    void skipsExcludedProvider() {
        RoutingAiGenerator generator = new RoutingAiGenerator(
                List.of(fake("claude", true, "c"), fake("gemini", true, "g")), List.of("claude", "gemini"));

        assertThat(generator.generate(request.excluding("claude")).provider()).isEqualTo("gemini");
        assertThat(calls).containsExactly("gemini");
        assertThatThrownBy(() -> generator.generate(request.excluding("claude").excluding("gemini")))
                .isInstanceOf(ServiceUnavailableException.class);
    }

    @Test
    @DisplayName("모든 제공자를 쓸 수 없으면 503, 모두 실패하면 마지막 오류를 던진다")
    void allProvidersFail() {
        RoutingAiGenerator none = new RoutingAiGenerator(
                List.of(fake("claude", false, null), fake("gemini", false, null)), List.of("claude", "gemini"));
        assertThat(none.hasAvailableProvider()).isFalse();
        assertThatThrownBy(() -> none.generate(request)).isInstanceOf(ServiceUnavailableException.class);

        RoutingAiGenerator failing = new RoutingAiGenerator(
                List.of(fake("claude", true, null), fake("gemini", true, null)), List.of("claude", "gemini"));
        assertThatThrownBy(() -> failing.generate(request)).hasMessageContaining("gemini 실패");
    }

    @Test
    @DisplayName("알 수 없는 제공자 이름이면 기동 시 실패한다")
    void rejectsUnknownProvider() {
        assertThatThrownBy(() -> new RoutingAiGenerator(List.of(fake("claude", true, "c")), List.of("gpt")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("gpt");
    }

    /**
     * result 가 null 이면 호출 시 실패한다.
     */
    private AiProvider fake(String name, boolean available, String result) {
        return new AiProvider() {
            @Override
            public String name() {
                return name;
            }

            @Override
            public boolean isAvailable() {
                return available;
            }

            @Override
            @SuppressWarnings("unchecked")
            public <T> T generate(AiRequest<T> request) {
                calls.add(name);
                if (result == null) {
                    throw new IllegalStateException(name + " 실패");
                }
                return (T) result;
            }
        };
    }
}
