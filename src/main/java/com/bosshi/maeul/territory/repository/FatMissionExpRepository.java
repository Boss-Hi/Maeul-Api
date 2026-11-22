package com.bosshi.maeul.territory.repository;

import com.bosshi.maeul.territory.entity.FatMissionExp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FatMissionExpRepository extends JpaRepository<FatMissionExp, Long> {

    boolean existsByUserIdAndItineraryMissionId(Long userId, Long itineraryMissionId);

    @Query("select coalesce(sum(m.exp), 0) from FatMissionExp m where m.fatId = :fatId")
    long sumExpByFatId(@Param("fatId") Long fatId);
}
