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

    @Test
    @DisplayName("주축제가 화성이면 앵커 장소와 주축제를 프롬프트에 넣고, 아니면 넣지 않는다")
    void anchorPlacesOnlyForHwaseongFestival() {
        ItineraryGenerateDTO hwaseong = dto();
        hwaseong.setTour(festival("41", "590"));
        ItineraryGenerateDTO hwaseongDistrict = dto();
        hwaseongDistrict.setTour(festival("41", "597"));
        hwaseong.setRecommendableTours(List.of(
                Tour.builder().contentId("A1").title("화성 식당").addr1("경기도 화성시 동탄구").build(),
                Tour.builder().contentId("A3").title("화성 호텔").contentTypeId("32").addr1("경기도 화성시 향남읍").build(),
                Tour.builder().contentId("A2").title("수원 공연장").addr1("경기도 수원시 팔달구").build()));
        ItineraryGenerateDTO other = dto();
        other.setTour(festival("41", "111"));

        String hwaseongPrompt = generator.buildItineraryPrompt(hwaseong);
        String districtPrompt = generator.buildItineraryPrompt(hwaseongDistrict);
        String otherPrompt = generator.buildItineraryPrompt(other);

        AnchorPlaces.HWASEONG.forEach(place -> {
            assertThat(hwaseongPrompt).contains(place.name());
            assertThat(districtPrompt).contains(place.name());
            assertThat(otherPrompt).doesNotContain(place.name());
        });
        assertThat(hwaseongPrompt).contains("contentId: FEST1").contains("화성 식당").doesNotContain("수원 공연장").doesNotContain("화성 호텔");
    }

    @Test
    @DisplayName("화성 일정의 앵커 장소는 NORMAL 로, 주축제는 TOUR 로 조립한다")
    void assemblesAnchorAsNormalAndFestivalAsTour() {
        ItineraryGenerateDTO dto = dto();
        dto.setTour(festival("41", "590"));
        when(aiGenerator.generate(any())).thenReturn(new AiResult<>("claude", output(
                new ItineraryTourJson("", 1, "정조효공원·융건릉", "짧은 역사 미션", "10:00", "11:00"),
                new ItineraryTourJson("FEST1", 2, "", "", "13:00", "15:00"))));

        Itinerary itinerary = generator.generateItinerary(dto);

        assertThat(itinerary.getItineraryDays().get(0).getItinerarySchedules())
                .extracting("type")
                .containsExactly("NORMAL", "TOUR");
    }

    @Test
    @DisplayName("화성 일정에서 같은 날 같은 앵커가 반복되면 다음 제공자로 다시 요청하고, 남은 제공자가 없으면 그 일정을 쓴다")
    void retriesOnRepeatedAnchorAndFallsBack() {
        ItineraryGenerateDTO dto = dto();
        dto.setTour(festival("41", "590"));
        ItineraryAiOutput repeated = output(
                new ItineraryTourJson("", 1, "황금해안길 산책", "걷기", "10:00", "11:00"),
                new ItineraryTourJson("", 2, "황금해안길 낙조 걷기", "낙조", "17:00", "18:00"));
        ItineraryAiOutput clean = output(
                new ItineraryTourJson("", 1, "황금해안길 낙조 걷기", "낙조", "17:00", "18:00"));
        when(aiGenerator.generate(any()))
                .thenReturn(new AiResult<>("claude", repeated))
                .thenReturn(new AiResult<>("gemini", clean));

        Itinerary retried = generator.generateItinerary(dto);

        assertThat(retried.getItineraryDays().get(0).getItinerarySchedules()).hasSize(1);

        when(aiGenerator.generate(any()))
                .thenReturn(new AiResult<>("claude", repeated))
                .thenThrow(new ServiceUnavailableException("none"));

        Itinerary fallback = generator.generateItinerary(dto);

        assertThat(fallback.getItineraryDays().get(0).getItinerarySchedules()).hasSize(2);
    }

    private static Tour festival(String regionCode, String code) {
        return Tour.builder().contentId("FEST1").title("화성 축제")
                .lDongRegnCd(regionCode).lDongSignguCd(code).build();
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
