package com.bosshi.maeul.openapi.service;

import com.bosshi.maeul.openapi.entity.Festival;
import com.bosshi.maeul.openapi.repository.FestivalRepository;
import com.bosshi.maeul.openapi.request.SearchFestivalRequest;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.modulith.NamedInterface;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@NamedInterface
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FestivalService {
    private final FestivalRepository festivalRepository;

    /**
     * 모든 축제 목록을 조회합니다.
     */
    public List<Festival> all() {
        return festivalRepository.findAll();
    }

    /**
     * SearchFestivalRequest 조건에 맞춰 축제를 검색합니다.
     */
    public List<Festival> search(SearchFestivalRequest request) {
        Specification<Festival> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (request.getAreaCode() != null && !request.getAreaCode().isBlank()) {
                predicates.add(cb.equal(root.get("areaCode"), request.getAreaCode()));
            }
            if (request.getSigunguCode() != null && !request.getSigunguCode().isBlank()) {
                predicates.add(cb.equal(root.get("sigunguCode"), request.getSigunguCode()));
            }
            if (request.getCat1() != null && !request.getCat1().isBlank()) {
                predicates.add(cb.equal(root.get("cat1"), request.getCat1()));
            }
            if (request.getCat2() != null && !request.getCat2().isBlank()) {
                predicates.add(cb.equal(root.get("cat2"), request.getCat2()));
            }
            if (request.getCat3() != null && !request.getCat3().isBlank()) {
                predicates.add(cb.equal(root.get("cat3"), request.getCat3()));
            }
            if (request.getLDongRegnCd() != null && !request.getLDongRegnCd().isBlank()) {
                predicates.add(cb.equal(root.get("lDongRegnCd"), request.getLDongRegnCd()));
            }
            if (request.getLDongSigunguCd() != null && !request.getLDongSigunguCd().isBlank()) {
                predicates.add(cb.equal(root.get("lDongSignguCd"), request.getLDongSigunguCd()));
            }
            if (request.getLclsSystm1() != null && !request.getLclsSystm1().isBlank()) {
                predicates.add(cb.equal(root.get("lclsSystm1"), request.getLclsSystm1()));
            }
            if (request.getLclsSystm2() != null && !request.getLclsSystm2().isBlank()) {
                predicates.add(cb.equal(root.get("lclsSystm2"), request.getLclsSystm2()));
            }
            if (request.getLclsSystm3() != null && !request.getLclsSystm3().isBlank()) {
                predicates.add(cb.equal(root.get("lclsSystm3"), request.getLclsSystm3()));
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
}
