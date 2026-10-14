package com.bosshi.maeul.openapi.service;

import com.bosshi.maeul.openapi.entity.Tour;
import com.bosshi.maeul.openapi.repository.TourCategoryRepository;
import com.bosshi.maeul.openapi.repository.TourRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FestivalService 테스트")
class FestivalServiceTest {

    @Mock
    private TourRepository festivalRepository;

    @Mock
    private TourCategoryRepository tourCategoryRepository;

    @InjectMocks
    private FestivalService festivalService;

    @Test
    @DisplayName("popular은 contentTypeId=15 기준 랜덤 축제 10개를 조회한다")
    void popularReturnsRandomFestivalTours() {
        // Given
        List<Tour> expected = List.of(
                Tour.builder().contentId("1").title("축제1").build(),
                Tour.builder().contentId("2").title("축제2").build()
        );
        when(festivalRepository.findRandomToursWhereContentTypeIdIs("15", 10)).thenReturn(expected);

        // When
        List<Tour> result = festivalService.popular();

        // Then
        assertThat(result).isEqualTo(expected);
        verify(festivalRepository).findRandomToursWhereContentTypeIdIs("15", 10);
    }
}

