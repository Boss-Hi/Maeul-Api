package com.bosshi.maeul.category.repository;

import com.bosshi.maeul.openapi.entity.TourContentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.modulith.NamedInterface;

import java.util.List;
import java.util.Optional;

@NamedInterface
public interface TourContentTypeRepository extends JpaRepository<TourContentType, Long> {
    Optional<TourContentType> findByName(String name);
    Optional<TourContentType> findByContentTypeId(String contentTypeId);
    Optional<TourContentType> findByContentTypeIdMultiLang(String contentTypeIdMultiLang);
    List<TourContentType> findAllByActive(Boolean active);
}
