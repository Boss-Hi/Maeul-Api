package com.bosshi.maeul.itineraries.itinerary.generator;

import com.anthropic.models.messages.StructuredOutputConfig;
import com.bosshi.maeul.ai.AiGenerator;
import com.bosshi.maeul.ai.AiRequest;
import com.bosshi.maeul.ai.AiResult;
import com.bosshi.maeul.common.exception.ServiceUnavailableException;
import com.bosshi.maeul.itineraries.itinerary.dto.ItineraryGenerateDTO;
import com.bosshi.maeul.itineraries.itinerary.entity.Itinerary;
import com.bosshi.maeul.itineraries.itinerary.generator.AiItineraryGenerator.ItineraryAiOutput;
import com.bosshi.maeul.itineraries.itinerary.generator.impl.ItineraryGeneratorImpl.ItineraryDayJson;
import com.bosshi.maeul.itineraries.itinerary.generator.impl.ItineraryGeneratorImpl.ItineraryTourJson;
import com.bosshi.maeul.itineraries.openapi.entity.Tour;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("AiItineraryGenerator 테스트")
class AiItineraryGeneratorTest {

    private final AiGenerator aiGenerator = mock(AiGenerator.class);
    private final AiItineraryGenerator generator = new AiItineraryGenerator(aiGenerator);

    @Test
    @DisplayName("응답 타입에서 structured outputs 스키마를 만들 수 있다 (SDK 로컬 검증 통과)")
    void outputTypeProducesValidSchema() {
        assertThatCode(() -> StructuredOutputConfig.<ItineraryAiOutput>builder()
                .format(ItineraryAiOutput.class)
                .build())
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("AI 응답으로 TOUR/NORMAL 상세 일정을 조립하고 시간은 하루 시간대 안에서만 담는다")
    void buildsItineraryFromAiOutput() {
        when(aiGenerator.generate(any())).thenReturn(new AiResult<>("claude", output(
                new ItineraryTourJson("123", 1, "", "", "10:00", "11:00"),
                new ItineraryTourJson("", 2, "점심", "국밥", "23:00", "23:30"))));

        Itinerary itinerary = generator.generateItinerary(dto());

        assertThat(itinerary.getItineraryDays()).hasSize(1);
        assertThat(itinerary.getItineraryDays().get(0).getItinerarySchedules())
                .extracting("type", "startTime")
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("TOUR", LocalTime.of(10, 0)),
                        org.assertj.core.groups.Tuple.tuple("NORMAL", null));
    }

    @Test
    @DisplayName("일정이 비어 있는 응답이면 그 제공자를 빼고 다시 요청한다")
    void retriesWithoutProviderWhenResultUnusable() {
        when(aiGenerator.generate(any()))
                .thenReturn(new AiResult<>("claude", new ItineraryAiOutput(List.of())))
                .thenReturn(new AiResult<>("gemini", output(new ItineraryTourJson("123", 1, null, null, null, null))));

        Itinerary itinerary = generator.generateItinerary(dto());

        assertThat(itinerary.getItineraryDays()).hasSize(1);
        org.mockito.ArgumentCaptor<AiRequest<ItineraryAiOutput>> requests = org.mockito.ArgumentCaptor.forClass(AiRequest.class);
        org.mockito.Mockito.verify(aiGenerator, org.mockito.Mockito.times(2)).generate(requests.capture());
        assertThat(requests.getAllValues().get(0).excludedProviders()).isEmpty();
        assertThat(requests.getAllValues().get(1).excludedProviders()).containsExactly("claude");
    }

    @Test
    @DisplayName("남은 제공자가 없으면 앞서 결과를 쓰지 못한 원인을 던진다")
    void throwsEarlierCauseWhenNoProviderLeft() {
        when(aiGenerator.generate(any()))
                .thenReturn(new AiResult<>("claude", new ItineraryAiOutput(List.of())))
                .thenThrow(new ServiceUnavailableException("none"));

        assertThatThrownBy(() -> generator.generateItinerary(dto()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("claude 응답에 일정이 없습니다");
    }

    @Test
    @DisplayName("추천 관광지가 없으면 AI 를 호출하지 않고 실패한다")
    void failsWithoutRecommendableTours() {
        ItineraryGenerateDTO dto = dto();
        dto.setRecommendableTours(List.of());

        assertThatThrownBy(() -> generator.generateItinerary(dto)).isInstanceOf(IllegalStateException.class);
        org.mockito.Mockito.verifyNoInteractions(aiGenerator);
    }

    private static ItineraryAiOutput output(ItineraryTourJson... items) {
        return new ItineraryAiOutput(List.of(new ItineraryDayJson("2026-11-10", 1, List.of(items))));
    }

    private static ItineraryGenerateDTO dto() {
        ItineraryGenerateDTO dto = ItineraryGenerateDTO.builder()
                .startDate(LocalDate.of(2026, 11, 10)).endDate(LocalDate.of(2026, 11, 10))
                .dayStartTime(LocalTime.of(9, 30)).dayEndTime(LocalTime.of(22, 30))
                .userId(1L).build();
        dto.setRecommendableTours(List.of(Tour.builder().contentId("123").title("축제").build()));
        return dto;
    }
}
