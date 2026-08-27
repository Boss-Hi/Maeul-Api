package com.bosshi.maeul.plan.repository;

import com.bosshi.maeul.plan.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TripRepository extends JpaRepository<Trip, String> {
    Optional<Trip> findById(String id);
}

