package com.bosshi.maeul.territory.repository;

import com.bosshi.maeul.territory.entity.ItineraryCompletion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface ItineraryCompletionRepository extends JpaRepository<ItineraryCompletion, Long> {

    boolean existsByItineraryIdIn(Collection<Long> itineraryIds);
}
