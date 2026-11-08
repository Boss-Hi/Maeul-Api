package com.bosshi.maeul.itinerary.generator;

import com.anthropic.models.messages.StructuredOutputConfig;
import com.bosshi.maeul.ai.service.ClaudeService;
import com.bosshi.maeul.itinerary.dto.ItineraryGenerateDTO;
import com.bosshi.maeul.itinerary.entity.Itinerary;
import com.bosshi.maeul.itinerary.generator.ClaudeItineraryGenerator.ClaudeItineraryOutput;
import com.bosshi.maeul.itinerary.generator.impl.ItineraryGeneratorImpl.ItineraryDayJson;
import com.bosshi.maeul.itinerary.generator.impl.ItineraryGeneratorImpl.ItineraryTourJson;
import com.bosshi.maeul.openapi.entity.Tour;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("ClaudeItineraryGenerator 테스트")
class ClaudeItineraryGeneratorTest {

    @Test
    @DisplayName("응답 타입에서 structured outputs 스키마를 만들 수 있다 (SDK 로컬 검증 통과)")
    void outputTypeProducesValidSchema() {
        assertThatCode(() -> StructuredOutputConfig.<ClaudeItineraryOutput>builder()
                .format(ClaudeItineraryOutput.class)
                .build())
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Claude 응답으로 TOUR/NORMAL 상세 일정을 조립한다")
    void buildsItineraryFromClaudeOutput() throws Exception {
        ClaudeService claudeService = mock(ClaudeService.class);
        when(claudeService.generate(anyString(), eq(ClaudeItineraryOutput.class))).thenReturn(new ClaudeItineraryOutput(List.of(
                new ItineraryDayJson("2026-11-10", 1, List.of(
                        new ItineraryTourJson("123", 1, "", "", null, null),
                        new ItineraryTourJson("", 2, "점심", "국밥", null, null))))));

        Tour tour = Tour.builder().contentId("123").title("축제").build();
        ItineraryGenerateDTO dto = ItineraryGenerateDTO.builder()
                .startDate(LocalDate.of(2026, 11, 10)).endDate(LocalDate.of(2026, 11, 10))
                .userId(1L).build();
        dto.setRecommendableTours(List.of(tour));

        Itinerary itinerary = new ClaudeItineraryGenerator(claudeService).generateItinerary(dto);

        assertThat(itinerary.getItineraryDays()).hasSize(1);
        assertThat(itinerary.getItineraryDays().get(0).getItinerarySchedules())
                .extracting("type")
                .containsExactly("TOUR", "NORMAL");
    }
}
