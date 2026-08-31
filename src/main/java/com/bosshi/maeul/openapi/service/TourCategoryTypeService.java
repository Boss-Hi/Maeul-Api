package com.bosshi.maeul.openapi.service;

import com.bosshi.maeul.openapi.repository.TourCategoryTypeRepository;
import com.bosshi.maeul.openapi.response.TourCategoryTypeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.NamedInterface;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@NamedInterface
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TourCategoryTypeService {
    private final TourCategoryTypeRepository tourCategoryTypeRepository;

    /**
     * 모든 관광 카테고리 목록을 조회합니다.
     */
    public List<TourCategoryTypeResponse> all() {
        return tourCategoryTypeRepository.findAll().stream()
                .map(TourCategoryTypeResponse::from)
                .toList();
    }
}
