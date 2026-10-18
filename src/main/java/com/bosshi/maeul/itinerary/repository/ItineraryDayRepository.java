package com.bosshi.maeul.itinerary.repository;

import com.bosshi.maeul.itinerary.entity.ItineraryDay;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ItineraryDayRepository extends JpaRepository<ItineraryDay, Long> {
    @NonNull Optional<ItineraryDay> findById(Long id);
}

