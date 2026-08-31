package com.bosshi.maeul.category.service;

import com.bosshi.maeul.category.repository.TourCategoryRepository;
import com.bosshi.maeul.category.response.TourCategoryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.NamedInterface;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public List<TourCategoryResponse> getAllTourCategories() {
        return tourCategoryRepository.findAll().stream()
                .map(TourCategoryResponse::from)
                .toList();
    }
}
