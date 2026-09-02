package com.bosshi.maeul.openapi.service;

import com.bosshi.maeul.openapi.entity.TourCategory;
import com.bosshi.maeul.openapi.repository.TourCategoryRepository;
import com.bosshi.maeul.openapi.response.TourCategoryResponse;
import com.bosshi.maeul.openapi.response.TourCategoryTreeResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TourCategoryServiceTest {

    @Mock
    private TourCategoryRepository tourCategoryRepository;

    @InjectMocks
    private TourCategoryService tourCategoryService;

    @Test
    void allShouldReturnFlatResponseAndBuildTreeWorksCorrectly() {
        // given
        TourCategory depth1 = TourCategory.builder()
                .code("AC")
                .name("숙박")
                .depth(1)
                .parentCode(null)
                .build();

        TourCategory depth2_1 = TourCategory.builder()
                .code("AC01")
                .name("호텔")
                .depth(2)
                .parentCode("AC")
                .build();

        TourCategory depth2_2 = TourCategory.builder()
                .code("AC02")
                .name("콘도미니엄")
                .depth(2)
                .parentCode("AC")
                .build();

        TourCategory depth3 = TourCategory.builder()
                .code("AC010100")
                .name("호텔 상세")
                .depth(3)
                .parentCode("AC01")
                .build();

        when(tourCategoryRepository.findAll()).thenReturn(List.of(depth1, depth2_1, depth2_2, depth3));

        // when
        List<TourCategoryResponse> flatResult = tourCategoryService.all();
        List<TourCategoryTreeResponse> treeResult = TourCategoryTreeResponse.buildTree(flatResult);

        // then
        assertThat(flatResult).hasSize(4);
        
        assertThat(treeResult).hasSize(1);
        TourCategoryTreeResponse parentResponse = treeResult.get(0);
        assertThat(parentResponse.id()).isEqualTo("ac");
        assertThat(parentResponse.name()).isEqualTo("숙박");
        assertThat(parentResponse.children()).hasSize(2);

        TourCategoryTreeResponse.ChildResponse childResponse1 = parentResponse.children().get(0);
        assertThat(childResponse1.id()).isEqualTo("ac01");
        assertThat(childResponse1.name()).isEqualTo("호텔");

        TourCategoryTreeResponse.ChildResponse childResponse2 = parentResponse.children().get(1);
        assertThat(childResponse2.id()).isEqualTo("ac02");
        assertThat(childResponse2.name()).isEqualTo("콘도미니엄");
    }
}
