package com.bosshi.maeul.itineraries.itinerary.photomission;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

interface PhotoMissionAttemptRepository extends JpaRepository<PhotoMissionAttempt, Long> {

    long countByPhotoMissionIdAndAttemptedOn(Long photoMissionId, LocalDate attemptedOn);

    boolean existsByUserIdAndImageHash(Long userId, String imageHash);
}
