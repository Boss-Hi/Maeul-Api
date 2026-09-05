package com.bosshi.maeul.openapi.repository;

import com.bosshi.maeul.openapi.entity.Tour;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.modulith.NamedInterface;

import java.util.Optional;

@NamedInterface
public interface TourRepository extends JpaRepository<Tour, String>, JpaSpecificationExecutor<Tour> {
    Optional<Tour> findByContentId(String contentId);
}
