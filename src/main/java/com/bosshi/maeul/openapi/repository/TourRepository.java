package com.bosshi.maeul.openapi.repository;

import com.bosshi.maeul.openapi.entity.Tour;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.modulith.NamedInterface;

@NamedInterface
public interface TourRepository extends JpaRepository<Tour, String>, JpaSpecificationExecutor<Tour> {
    Tour findByContentId(Long contentId);
}
