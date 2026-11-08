package com.bosshi.maeul.itineraries.itinerary.repository;

import com.bosshi.maeul.itineraries.itinerary.entity.Mission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MissionRepository extends JpaRepository<Mission, Long> {
    boolean existsByCode(String code);
}

