package com.bosshi.maeul.openapi.service;

import com.bosshi.maeul.openapi.entity.Festival;
import com.bosshi.maeul.openapi.repository.FestivalRepository;
import com.bosshi.maeul.openapi.request.SearchFestivalRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FestivalServiceTest {

    @Mock
    private FestivalRepository festivalRepository;

    @InjectMocks
    private FestivalService festivalService;

    @Test
    void searchShouldCallRepositoryWithCorrectSpecificationAndPageable() {
        // given
        SearchFestivalRequest request = SearchFestivalRequest.builder()
                .areaCode("1")
                .sigunguCode("10")
                .eventStartDate("20260901")
                .eventEndDate("20260930")
                .pageNo(1)
                .numOfRows(5)
                .build();

        Festival festival = Festival.builder()
                .contentId("111")
                .title("테스트 축제")
                .build();

        when(festivalRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(festival)));

        // when
        List<Festival> result = festivalService.search(request);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("테스트 축제");

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(festivalRepository).findAll(any(Specification.class), pageableCaptor.capture());

        Pageable pageable = pageableCaptor.getValue();
        assertThat(pageable.getPageNumber()).isEqualTo(0);
        assertThat(pageable.getPageSize()).isEqualTo(5);
    }
}
