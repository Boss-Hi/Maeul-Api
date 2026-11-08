package com.bosshi.maeul.itineraries.openapi.service;

import com.bosshi.maeul.itineraries.openapi.entity.TourCategory;
import com.bosshi.maeul.itineraries.openapi.repository.TourCategoryRepository;
import com.bosshi.maeul.itineraries.openapi.type.FestivalCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.NamedInterface;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@NamedInterface
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TourCategoryService {
    private final TourCategoryRepository tourCategoryRepository;

    /**
     * 모든 관광 카테고리 목록을 조회합니다.
     */
    public List<TourCategory> all() {
        return tourCategoryRepository.findAll();
    }

    public List<TourCategory> festivalCategories() {
        return tourCategoryRepository.findByCodeIn(Arrays.stream(FestivalCategory.values()).map(FestivalCategory::getCode).toList());
    }
}
