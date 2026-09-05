package com.bosshi.maeul.openapi.service;

import com.bosshi.maeul.openapi.entity.Tour;
import com.bosshi.maeul.openapi.entity.TourCategory;
import com.bosshi.maeul.openapi.repository.TourCategoryRepository;
import com.bosshi.maeul.openapi.repository.TourRepository;
import com.bosshi.maeul.openapi.request.SearchFestivalRequest;
import com.bosshi.maeul.openapi.type.FestivalCategory;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.modulith.NamedInterface;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@NamedInterface
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FestivalService {
    private final TourRepository festivalRepository;
    private final TourCategoryRepository tourCategoryRepository;

    /**
     * 모든 축제 목록을 조회합니다.
     */
    public List<Tour> all() {
        return festivalRepository.findAll();
    }

    /**
     * SearchFestivalRequest 조건에 맞춰 축제를 검색합니다.
     */
    public List<Tour> search(SearchFestivalRequest request) {
        TourCategory category = null;
        if (request.getTourCategoryCode() != null) {
            category = tourCategoryRepository.findByCode(request.getTourCategoryCode())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리 코드입니다."));

            // FestivalCategory에 존재하는 코드인지 검증
            final String code = category.getCode();
            boolean isValid = Arrays.stream(FestivalCategory.values())
                    .anyMatch(fc -> fc.getCode().equalsIgnoreCase(code));

            if (!isValid) {
                throw new IllegalArgumentException("올바른 카테고리가 아닙니다.");
            }
        }

        final TourCategory finalCategory = category;

        Specification<Tour> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (request.getLDongRegnCd() != null && !request.getLDongRegnCd().isBlank()) {
                predicates.add(cb.equal(root.get("lDongRegnCd"), request.getLDongRegnCd()));
            }
            if (request.getLDongSigunguCd() != null && !request.getLDongSigunguCd().isBlank()) {
                predicates.add(cb.equal(root.get("lDongSignguCd"), request.getLDongSigunguCd()));
            }

            // tourCategoryId 기반 검색 필터링 추가
            if (finalCategory != null) {
                String categoryCode = finalCategory.getCode();
                Integer depth = finalCategory.getDepth();
                if (depth == 1) {
                    predicates.add(cb.equal(root.get("lclsSystm1"), categoryCode));
                } else if (depth == 2) {
                    predicates.add(cb.equal(root.get("lclsSystm2"), categoryCode));
                } else if (depth == 3) {
                    predicates.add(cb.equal(root.get("lclsSystm3"), categoryCode));
                }
            }

            // Date range filtering
            if (request.getEventStartDate() != null && !request.getEventStartDate().isBlank()) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("eventEndDate"), request.getEventStartDate()));
            }
            if (request.getEventEndDate() != null && !request.getEventEndDate().isBlank()) {
                predicates.add(cb.lessThanOrEqualTo(root.get("eventStartDate"), request.getEventEndDate()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        int page = request.getPageNo() != null ? request.getPageNo() - 1 : 0;
        int size = request.getNumOfRows() != null ? request.getNumOfRows() : 10;
        if (page < 0) page = 0;
        if (size <= 0) size = 10;
        Pageable pageable = PageRequest.of(page, size);

        return festivalRepository.findAll(spec, pageable).getContent();
    }

    public Tour findByContentId(Long contentId) {
        return festivalRepository.findByContentId(contentId);
    }
}
