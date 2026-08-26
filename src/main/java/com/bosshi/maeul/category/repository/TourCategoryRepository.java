package com.bosshi.maeul.category.repository;

import com.bosshi.maeul.category.domain.TourCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.modulith.NamedInterface;

import java.util.List;
import java.util.Optional;

@NamedInterface
public interface TourCategoryRepository extends JpaRepository<TourCategory, String> {
    Optional<TourCategory> findByCode(String code);
    Optional<TourCategory> findByName(String name);
    Optional<TourCategory> findByContentTypeId(String contentTypeId);
    List<TourCategory> findAllByActive(Boolean active);
}
