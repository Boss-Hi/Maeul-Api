package com.bosshi.maeul.itinerary.repository;

import com.bosshi.maeul.itinerary.entity.MissionTaskVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MissionVerificationRepository extends JpaRepository<MissionTaskVerification, Long> {
}

