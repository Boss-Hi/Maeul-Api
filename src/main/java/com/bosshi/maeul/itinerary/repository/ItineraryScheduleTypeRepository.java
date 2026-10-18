package com.bosshi.maeul.itinerary.repository;

import com.bosshi.maeul.itinerary.entity.ItineraryScheduleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItineraryScheduleTypeRepository extends JpaRepository<ItineraryScheduleType, String> {
}

