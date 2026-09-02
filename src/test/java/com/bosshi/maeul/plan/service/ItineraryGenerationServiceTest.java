package com.bosshi.maeul.plan.service;

import com.bosshi.maeul.ai.response.GeminiGenerateResponse;
import com.bosshi.maeul.ai.service.GeminiService;
import com.bosshi.maeul.openapi.entity.Tour;
import com.bosshi.maeul.plan.dto.ItineraryGenerateDTO;
import com.bosshi.maeul.plan.entity.Itinerary;
import com.bosshi.maeul.plan.repository.ItineraryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItineraryGenerationServiceTest {

    @Mock
    private ItineraryRepository itineraryRepository;

    @Mock
    private GeminiService geminiService;

    @InjectMocks
    private ItineraryGenerationService itineraryGenerationService;

    @Test
    void generateItineraryShouldParseJsonAndSaveItineraryToDb() {
        // given
        Tour tour = Tour.builder()
                .contentId("tour-123")
                .title("중심 축제")
                .addr1("부산광역시")
                .build();

        ItineraryGenerateDTO dto = ItineraryGenerateDTO.builder()
                .tour(tour)
                .startDate(LocalDate.of(2026, 9, 1))
                .endDate(LocalDate.of(2026, 9, 3))
                .selectedCategories(List.of("VE06"))
                .recommendableTours(List.of(
                        Tour.builder().contentId("130699").title("관광지1").build()
                ))
                .build();

        String apiResponseJson = "[\n" +
                "  {\n" +
                "    \"date\": \"2026-09-01\",\n" +
                "    \"dayNumber\": 1,\n" +
                "    \"items\": [\n" +
                "      {\n" +
                "        \"contentId\": \"130699\",\n" +
                "        \"sequence\": 1\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "]";

        GeminiGenerateResponse mockResponse = new GeminiGenerateResponse();
        GeminiGenerateResponse.Candidate candidate = new GeminiGenerateResponse.Candidate();
        GeminiGenerateResponse.Content content = new GeminiGenerateResponse.Content();
        GeminiGenerateResponse.Part part = new GeminiGenerateResponse.Part();
        part.setText(apiResponseJson);
        content.setParts(List.of(part));
        candidate.setContent(content);
        mockResponse.setCandidates(List.of(candidate));

        when(geminiService.generate(anyString())).thenReturn(mockResponse);
        when(itineraryRepository.save(any(Itinerary.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Itinerary result = itineraryGenerationService.generateItinerary(dto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTour().getContentId()).isEqualTo("tour-123");
        assertThat(result.getStartDate()).isEqualTo(LocalDate.of(2026, 9, 1));
        assertThat(result.getEndDate()).isEqualTo(LocalDate.of(2026, 9, 3));
        assertThat(result.getItineraryDays()).hasSize(1);
        assertThat(result.getItineraryDays().get(0).getDate()).isEqualTo(LocalDate.of(2026, 9, 1));
        assertThat(result.getItineraryDays().get(0).getVenues()).hasSize(1);
        assertThat(result.getItineraryDays().get(0).getVenues().get(0).getContentId()).isEqualTo("130699");
        assertThat(result.getItineraryDays().get(0).getVenues().get(0).getSequence()).isEqualTo(1);

        ArgumentCaptor<Itinerary> itineraryCaptor = ArgumentCaptor.forClass(Itinerary.class);
        verify(itineraryRepository).save(itineraryCaptor.capture());
        Itinerary saved = itineraryCaptor.getValue();
        assertThat(saved.getItineraryDays()).hasSize(1);
        assertThat(saved.getItineraryDays().get(0).getVenues().get(0).getContentId()).isEqualTo("130699");
    }
}
