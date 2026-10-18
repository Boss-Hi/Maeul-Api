package com.bosshi.maeul.itinerary.service;

import com.bosshi.maeul.itinerary.entity.Itinerary;
import com.bosshi.maeul.itinerary.entity.ItineraryDay;
import com.bosshi.maeul.itinerary.entity.ItinerarySchedule;
import com.bosshi.maeul.itinerary.filter.TourFilteringService;
import com.bosshi.maeul.itinerary.generator.impl.ItineraryGeneratorImpl;
import com.bosshi.maeul.itinerary.repository.ItineraryRepository;
import com.bosshi.maeul.itinerary.request.ItineraryTourCreateRequest;
import com.bosshi.maeul.itinerary.request.ItineraryUpdateRequest;
import com.bosshi.maeul.openapi.entity.Tour;
import com.bosshi.maeul.openapi.repository.TourRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ItineraryService 일정 편집 테스트")
class ItineraryServiceScheduleEditTest {

    @Mock
    private TourRepository tourRepository;

    @Mock
    private TourFilteringService tourFilteringService;

    @Mock
    private ItineraryRepository itineraryRepository;

    @Mock
    private ItineraryGeneratorImpl itineraryGenerator;

    private ItineraryService itineraryService;

    @BeforeEach
    void setUp() {
        itineraryService = new ItineraryService(tourRepository, tourFilteringService, itineraryRepository, itineraryGenerator);
    }

    @Test
    @DisplayName("소유자가 아닌 사용자는 일정을 수정할 수 없다")
    void updateItineraryDeniedForNonOwner() {
        // Given
        Itinerary itinerary = Itinerary.builder()
                .id(1L)
                .userId(10L)
                .startDate(LocalDate.of(2026, 10, 20))
                .endDate(LocalDate.of(2026, 10, 22))
                .itineraryDays(new ArrayList<>())
                .build();
        when(itineraryRepository.findById(1L)).thenReturn(Optional.of(itinerary));

        ItineraryUpdateRequest request = new ItineraryUpdateRequest();
        request.setStartDate(LocalDate.of(2026, 10, 21));

        // When & Then
        assertThatThrownBy(() -> itineraryService.updateItinerary(1L, 999L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("수정 권한");
    }

    @Test
    @DisplayName("같은 일차에서 시간이 겹치는 일정 항목은 추가할 수 없다")
    void addTourItemRejectsOverlappedTime() {
        // Given
        Itinerary itinerary = createItineraryWithOneDayAndTour(1L, 10L, 100L, 1000L,
                LocalTime.of(10, 0), LocalTime.of(11, 0));
        when(itineraryRepository.findById(1L)).thenReturn(Optional.of(itinerary));

        Tour tour = Tour.builder().contentId("C-NEW").title("새 장소").build();
        when(tourRepository.findByContentId("C-NEW")).thenReturn(Optional.of(tour));

        ItineraryTourCreateRequest request = new ItineraryTourCreateRequest();
        request.setContentId("C-NEW");
        request.setSequence(2);
        request.setStartTime(LocalTime.of(10, 30));
        request.setEndTime(LocalTime.of(11, 30));

        // When & Then
        assertThatThrownBy(() -> itineraryService.addTourItem(1L, 100L, 10L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("겹치는 시간대");
    }

    @Test
    @DisplayName("시간 없이도 일정 항목을 추가할 수 있다")
    void addTourItemAllowsEmptyTime() {
        // Given
        Itinerary itinerary = createItineraryWithOneDayAndTour(1L, 10L, 100L, 1000L,
                LocalTime.of(10, 0), LocalTime.of(11, 0));
        when(itineraryRepository.findById(1L)).thenReturn(Optional.of(itinerary));

        Tour tour = Tour.builder().contentId("C-NEW").title("새 장소").build();
        when(tourRepository.findByContentId("C-NEW")).thenReturn(Optional.of(tour));
        when(itineraryRepository.save(any(Itinerary.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ItineraryTourCreateRequest request = new ItineraryTourCreateRequest();
        request.setContentId("C-NEW");
        request.setSequence(2);

        // When
        Itinerary result = itineraryService.addTourItem(1L, 100L, 10L, request);

        // Then
        List<ItinerarySchedule> tours = result.getItineraryDays().getFirst().getItineraryTours();
        ItinerarySchedule added = tours.stream()
                .filter(t -> "C-NEW".equals(t.getTour().getContentId()))
                .findFirst()
                .orElseThrow();

        assertThat(added.getStartTime()).isNull();
        assertThat(added.getEndTime()).isNull();
        verify(itineraryRepository).save(any(Itinerary.class));
    }

    private Itinerary createItineraryWithOneDayAndTour(
            Long itineraryId,
            Long userId,
            Long dayId,
            Long itineraryTourId,
            LocalTime startTime,
            LocalTime endTime
    ) {
        Itinerary itinerary = Itinerary.builder()
                .id(itineraryId)
                .userId(userId)
                .startDate(LocalDate.of(2026, 10, 20))
                .endDate(LocalDate.of(2026, 10, 22))
                .itineraryDays(new ArrayList<>())
                .build();

        ItineraryDay day = ItineraryDay.builder()
                .id(dayId)
                .itinerary(itinerary)
                .date(LocalDate.of(2026, 10, 20))
                .dayNumber(1)
                .itineraryTours(new ArrayList<>())
                .build();

        Tour tour = Tour.builder().contentId("C-EXIST").title("기존 장소").build();
        ItinerarySchedule itineraryTour = ItinerarySchedule.builder()
                .id(itineraryTourId)
                .itineraryDay(day)
                .tour(tour)
                .sequence(1)
                .startTime(startTime)
                .endTime(endTime)
                .build();

        day.getItineraryTours().add(itineraryTour);
        itinerary.getItineraryDays().add(day);

        return itinerary;
    }
}


