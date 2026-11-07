package com.bosshi.maeul.itinerary.generator;

import com.bosshi.maeul.common.exception.ServiceUnavailableException;
import com.bosshi.maeul.itinerary.dto.ItineraryGenerateDTO;
import com.bosshi.maeul.itinerary.entity.Itinerary;
import com.bosshi.maeul.itinerary.generator.impl.ItineraryGeneratorImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ItineraryGeneratorRouter 테스트")
class ItineraryGeneratorRouterTest {

    private final List<String> calls = new ArrayList<>();
    private final ItineraryGenerateDTO dto = new ItineraryGenerateDTO();

    @Test
    @DisplayName("앞 제공자가 실패하면 다음 제공자로 넘어간다")
    void failsOverToNextProvider() {
        Itinerary expected = new Itinerary();
        ItineraryGeneratorRouter router = new ItineraryGeneratorRouter(
                List.of(fake("claude", true, null), fake("gemini", true, expected)),
                List.of("claude", "gemini"));

        assertThat(router.generate(dto)).isSameAs(expected);
        assertThat(calls).containsExactly("claude", "gemini");
    }

    @Test
    @DisplayName("설정한 순서대로 시도한다")
    void followsConfiguredOrder() {
        Itinerary expected = new Itinerary();
        ItineraryGeneratorRouter router = new ItineraryGeneratorRouter(
                List.of(fake("claude", true, new Itinerary()), fake("gemini", true, expected)),
                List.of("gemini", "claude"));

        assertThat(router.generate(dto)).isSameAs(expected);
        assertThat(calls).containsExactly("gemini");
    }

    @Test
    @DisplayName("키가 없는 제공자는 호출하지 않고 건너뛴다")
    void skipsUnavailableProvider() {
        Itinerary expected = new Itinerary();
        ItineraryGeneratorRouter router = new ItineraryGeneratorRouter(
                List.of(fake("claude", false, new Itinerary()), fake("gemini", true, expected)),
                List.of("claude", "gemini"));

        assertThat(router.generate(dto)).isSameAs(expected);
        assertThat(calls).containsExactly("gemini");
    }

    @Test
    @DisplayName("모든 제공자를 쓸 수 없으면 503, 모두 실패하면 마지막 오류를 던진다")
    void allProvidersFail() {
        ItineraryGeneratorRouter none = new ItineraryGeneratorRouter(
                List.of(fake("claude", false, null), fake("gemini", false, null)), List.of("claude", "gemini"));
        assertThat(none.hasAvailableProvider()).isFalse();
        assertThatThrownBy(() -> none.generate(dto)).isInstanceOf(ServiceUnavailableException.class);

        ItineraryGeneratorRouter failing = new ItineraryGeneratorRouter(
                List.of(fake("claude", true, null), fake("gemini", true, null)), List.of("claude", "gemini"));
        assertThatThrownBy(() -> failing.generate(dto)).hasMessageContaining("gemini 실패");
    }

    @Test
    @DisplayName("알 수 없는 제공자 이름이면 기동 시 실패한다")
    void rejectsUnknownProvider() {
        assertThatThrownBy(() -> new ItineraryGeneratorRouter(List.of(fake("claude", true, null)), List.of("gpt")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("gpt");
    }

    /**
     * result 가 null 이면 호출 시 실패한다.
     */
    private ItineraryGeneratorImpl fake(String name, boolean available, Itinerary result) {
        return new ItineraryGeneratorImpl() {
            @Override
            public Itinerary generateItinerary(ItineraryGenerateDTO dto) {
                calls.add(name);
                if (result == null) {
                    throw new IllegalStateException(name + " 실패");
                }
                return result;
            }

            @Override
            public String providerName() {
                return name;
            }

            @Override
            public boolean isAvailable() {
                return available;
            }
        };
    }
}
