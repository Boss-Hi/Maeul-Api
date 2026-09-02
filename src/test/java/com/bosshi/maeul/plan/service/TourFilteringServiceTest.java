package com.bosshi.maeul.plan.service;

import com.bosshi.maeul.openapi.entity.Festival;
import com.bosshi.maeul.openapi.entity.TourCategory;
import com.bosshi.maeul.openapi.repository.FestivalRepository;
import com.bosshi.maeul.openapi.repository.TourCategoryRepository;
import com.bosshi.maeul.plan.dto.PlanGenerateDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TourFilteringServiceTest {

    @Mock
    private TourCategoryRepository categoryRepository;

    @Mock
    private FestivalRepository festivalRepository;

    @InjectMocks
    private TourFilteringService tourFilteringService;

    @Test
    void filterTourByCategoriesShouldFilterBasedOnSelectedCategoriesAndDistance() {
        // given
        PlanGenerateDTO dto = PlanGenerateDTO.builder()
                .selectedCategories(List.of("숙박", "호텔"))
                .festival(Festival.builder()
                        .contentId("main-fest")
                        .mapX(126.9780)
                        .mapY(37.5665)
                        .build())
                .filterSettings(PlanGenerateDTO.FilterSettingsRequest.builder()
                        .maxDistanceKm(10)
                        .build())
                .build();

        TourCategory cat1 = TourCategory.builder()
                .code("AC")
                .name("숙박")
                .depth(1)
                .build();

        TourCategory cat2 = TourCategory.builder()
                .code("AC01")
                .name("호텔")
                .depth(2)
                .build();

        Festival matchingFestival = Festival.builder()
                .contentId("match-fest")
                .title("매칭 호텔")
                .lclsSystm2("AC01")
                .mapX(126.9780)
                .mapY(37.5665) // 같은 위치
                .build();

        Festival tooFarFestival = Festival.builder()
                .contentId("far-fest")
                .title("너무 먼 호텔")
                .lclsSystm2("AC01")
                .mapX(129.0756) // 부산 (매우 멂)
                .mapY(35.1796)
                .build();

        Festival differentCategoryFestival = Festival.builder()
                .contentId("diff-fest")
                .title("체험 시설")
                .lclsSystm2("EX01")
                .mapX(126.9780)
                .mapY(37.5665)
                .build();

        when(categoryRepository.findAll()).thenReturn(List.of(cat1, cat2));
        when(festivalRepository.findAll()).thenReturn(List.of(matchingFestival, tooFarFestival, differentCategoryFestival));

        // when
        List<Festival> result = tourFilteringService.filterTourByCategories(dto);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getContentId()).isEqualTo("match-fest");
        assertThat(result.get(0).getTitle()).isEqualTo("매칭 호텔");
    }
}
