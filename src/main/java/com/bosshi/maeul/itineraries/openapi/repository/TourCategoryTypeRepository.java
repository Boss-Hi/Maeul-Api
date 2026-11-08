package com.bosshi.maeul.itineraries.openapi.repository;

import com.bosshi.maeul.itineraries.openapi.entity.TourCategoryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.modulith.NamedInterface;

import java.util.List;
import java.util.Optional;

@NamedInterface
public interface TourCategoryTypeRepository extends JpaRepository<TourCategoryType, Long> {
    Optional<TourCategoryType> findByName(String name);
    Optional<TourCategoryType> findByContentTypeId(String contentTypeId);
    Optional<TourCategoryType> findByContentTypeIdMultiLang(String contentTypeIdMultiLang);
    List<TourCategoryType> findAllByActive(Boolean active);
}
