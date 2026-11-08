package com.bosshi.maeul.itineraries.itinerary.service;

import com.bosshi.maeul.common.exception.ServiceUnavailableException;
import com.bosshi.maeul.itineraries.itinerary.client.SurveyAnswerClient;
import com.bosshi.maeul.itineraries.itinerary.dto.ItineraryGenerateDTO;
import com.bosshi.maeul.itineraries.itinerary.dto.SurveyAnswerInfo;
import com.bosshi.maeul.itineraries.itinerary.entity.Itinerary;
import com.bosshi.maeul.itineraries.itinerary.filter.TourFilteringService;
import com.bosshi.maeul.itineraries.itinerary.generator.ItineraryGeneratorRouter;
import com.bosshi.maeul.itineraries.itinerary.repository.ItineraryRepository;
import com.bosshi.maeul.itineraries.itinerary.type.Pace;
import com.bosshi.maeul.itineraries.openapi.entity.Tour;
import com.bosshi.maeul.itineraries.openapi.repository.TourRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("ItineraryService 일정 생성 설정 테스트")
class ItineraryServiceTest {

    private final SurveyAnswerClient surveyAnswerClient = mock(SurveyAnswerClient.class);
    private final ItineraryGeneratorRouter router = mock(ItineraryGeneratorRouter.class);
    private ItineraryService service;

    @BeforeEach
    void setUp() {
        TourRepository tourRepository = mock(TourRepository.class);
        when(tourRepository.findByContentId("1")).thenReturn(Optional.of(Tour.builder().contentId("1").build()));
        TourFilteringService filtering = mock(TourFilteringService.class);
        when(filtering.filterTourByCategories(any())).thenReturn(List.of());
        ItineraryRepository repository = mock(ItineraryRepository.class);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(router.generate(any())).thenReturn(Itinerary.builder().itineraryDays(new ArrayList<>()).build());
        service = new ItineraryService(tourRepository, filtering, repository, router, surveyAnswerClient);
    }

    @Test
    @DisplayName("요청한 페이스와 시간대를 그대로 쓰고, 설문 템포보다 요청이 우선한다")
    void requestValuesWin() throws Exception {
        when(surveyAnswerClient.findAnswers(7L)).thenReturn(List.of(answer("relaxed")));
        ItineraryGenerateDTO dto = dto(Pace.PACKED, LocalTime.of(8, 0), LocalTime.of(20, 0));

        service.generateItinerary(dto);

        assertThat(dto.getPace()).isEqualTo(Pace.PACKED);
        assertThat(dto.getFilterSettings().getMaxVenuesPerDay()).isNull();
        assertThat(dto.getDayStartTime()).isEqualTo(LocalTime.of(8, 0));
        assertThat(dto.getDayEndTime()).isEqualTo(LocalTime.of(20, 0));
    }

    @Test
    @DisplayName("요청에 페이스가 없으면 설문의 일정 템포를 쓴다")
    void surveyTempoFillsMissingPace() throws Exception {
        when(surveyAnswerClient.findAnswers(7L)).thenReturn(List.of(answer("coffee"), answer("relaxed")));
        ItineraryGenerateDTO dto = dto(null, null, null);

        service.generateItinerary(dto);

        assertThat(dto.getPace()).isEqualTo(Pace.RELAXED);
        assertThat(dto.getFilterSettings().getMaxVenuesPerDay()).isEqualTo(3);
        assertThat(dto.getSurveyAnswers()).hasSize(2);
    }

    @Test
    @DisplayName("페이스도 설문 템포도 없으면 MODERATE 와 기본 시간대 09:30~22:30 을 쓴다")
    void defaultsApply() throws Exception {
        when(surveyAnswerClient.findAnswers(7L)).thenReturn(List.of());
        ItineraryGenerateDTO dto = dto(null, null, null);

        service.generateItinerary(dto);

        assertThat(dto.getPace()).isEqualTo(Pace.MODERATE);
        assertThat(dto.getFilterSettings().getMaxVenuesPerDay()).isEqualTo(5);
        assertThat(dto.getDayStartTime()).isEqualTo(LocalTime.of(9, 30));
        assertThat(dto.getDayEndTime()).isEqualTo(LocalTime.of(22, 30));
    }

    @Test
    @DisplayName("설문 답변을 가져오지 못하면 일정 생성을 시작하지 않고 실패한다")
    void surveyFailureStopsGeneration() {
        when(surveyAnswerClient.findAnswers(7L)).thenThrow(new ServiceUnavailableException("설문 조회 실패"));

        assertThatThrownBy(() -> service.generateItinerary(dto(null, null, null)))
                .isInstanceOf(ServiceUnavailableException.class);
        verify(router, never()).generate(any());
    }

    private static SurveyAnswerInfo answer(String optionValue) {
        return new SurveyAnswerInfo("질문", "라벨", null, optionValue);
    }

    private static ItineraryGenerateDTO dto(Pace pace, LocalTime start, LocalTime end) {
        return ItineraryGenerateDTO.builder()
                .tourId("1").userId(7L)
                .startDate(LocalDate.of(2026, 11, 10)).endDate(LocalDate.of(2026, 11, 11))
                .pace(pace).dayStartTime(start).dayEndTime(end)
                .filterSettings(new ItineraryGenerateDTO.FilterSettingsRequest(5, 25, true))
                .build();
    }
}
