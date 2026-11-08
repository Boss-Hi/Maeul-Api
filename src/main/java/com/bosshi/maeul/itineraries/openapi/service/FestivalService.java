package com.bosshi.maeul.itineraries.openapi.service;

import com.bosshi.maeul.itineraries.openapi.entity.Tour;
import com.bosshi.maeul.itineraries.openapi.entity.TourCategory;
import com.bosshi.maeul.itineraries.openapi.repository.TourCategoryRepository;
import com.bosshi.maeul.itineraries.openapi.repository.TourRepository;
import com.bosshi.maeul.itineraries.openapi.request.SearchFestivalRequest;
import com.bosshi.maeul.itineraries.openapi.type.FestivalCategory;
import com.bosshi.maeul.itineraries.openapi.type.FestivalStatus;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.modulith.NamedInterface;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@NamedInterface
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FestivalService {
    private static final String FESTIVAL_CONTENT_TYPE_ID = "15";
    private static final int POPULAR_LIMIT = 10;
    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    private final TourRepository festivalRepository;
    private final TourCategoryRepository tourCategoryRepository;

    /**
     * 모든 축제 목록을 조회합니다.
     */
    public List<Tour> all() {
        return festivalRepository.findAll();
    }

    public List<Tour> popular() {
        return festivalRepository.findRandomToursWhereContentTypeIdIs(FESTIVAL_CONTENT_TYPE_ID, POPULAR_LIMIT);
    }

    public Page<Tour> search(SearchFestivalRequest request, Pageable pageable) {
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


            if (request.getLocation() != null) {
                if(request.getLocation().getRegion1depth() != null) {
                    predicates.add(cb.equal(root.get("lDongRegnCd"), request.getLocation().getRegion1depth()));
                }
                if(request.getLocation().getRegion2depth() != null) {
                    predicates.add(cb.equal(root.get("lDongSignguCd"), request.getLocation().getRegion2depth()));
                }
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

            // 진행 상태 필터링 (eventStartDate/eventEndDate 는 YYYYMMDD 문자열이므로 사전순 비교)
            FestivalStatus status = request.getStatus() != null ? request.getStatus() : FestivalStatus.ALL;
            String today = LocalDate.now(SEOUL).format(DateTimeFormatter.BASIC_ISO_DATE);
            switch (status) {
                case ONGOING -> {
                    predicates.add(cb.lessThanOrEqualTo(root.get("eventStartDate"), today));
                    predicates.add(cb.greaterThanOrEqualTo(root.get("eventEndDate"), today));
                }
                case ENDED -> predicates.add(cb.lessThan(root.get("eventEndDate"), today));
                case ALL -> { }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return festivalRepository.findAll(spec, pageable);
    }

    public Tour findByContentId(String contentId) {
        return festivalRepository.findByContentId(contentId)
                .orElseThrow(() -> new IllegalArgumentException("Tour을 찾을 수 없습니다: " + contentId));
    }
}
