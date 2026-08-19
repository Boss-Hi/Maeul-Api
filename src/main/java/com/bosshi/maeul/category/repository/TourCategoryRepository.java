package com.bosshi.maeul.category.repository;

import com.bosshi.maeul.category.domain.TourCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.modulith.NamedInterface;

@NamedInterface
public interface TourCategoryRepository extends JpaRepository<TourCategory, String> {
}
