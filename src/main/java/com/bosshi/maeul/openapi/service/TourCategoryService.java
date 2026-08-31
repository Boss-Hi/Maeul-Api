package com.bosshi.maeul.openapi.service;

import com.bosshi.maeul.openapi.repository.TourCategoryRepository;
import com.bosshi.maeul.openapi.response.TourCategoryResponse;
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
    public List<TourCategoryResponse> all() {
        return tourCategoryRepository.findAll().stream()
                .map(TourCategoryResponse::from)
                .toList();
    }
}
