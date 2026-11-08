package com.bosshi.maeul.itineraries.itinerary.repository;

import com.bosshi.maeul.itineraries.itinerary.entity.ItineraryScheduleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItineraryScheduleTypeRepository extends JpaRepository<ItineraryScheduleType, String> {
}

