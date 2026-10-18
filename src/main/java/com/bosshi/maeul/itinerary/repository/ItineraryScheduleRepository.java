package com.bosshi.maeul.itinerary.repository;

import com.bosshi.maeul.itinerary.entity.ItinerarySchedule;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ItineraryScheduleRepository extends JpaRepository<ItinerarySchedule, Long> {
    @NonNull Optional<ItinerarySchedule> findById(Long id);
}

