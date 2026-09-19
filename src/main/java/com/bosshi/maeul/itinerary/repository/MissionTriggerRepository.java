package com.bosshi.maeul.itinerary.repository;

import com.bosshi.maeul.itinerary.entity.MissionTrigger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MissionTriggerRepository extends JpaRepository<MissionTrigger, Long> {
}

