package com.bosshi.maeul.itineraries.itinerary.photomission;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface PhotoMissionPlaceRepository extends JpaRepository<PhotoMissionPlace, Long> {
    List<PhotoMissionPlace> findByEnabledTrueAndDeletedAtIsNull();
}
