package com.bosshi.maeul.territory.repository;

import com.bosshi.maeul.territory.entity.FatItineraryPeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FatItineraryPeriodRepository extends JpaRepository<FatItineraryPeriod, Long> {

    Optional<FatItineraryPeriod> findByItineraryId(Long itineraryId);

    List<FatItineraryPeriod> findByUserIdAndRegionCodeAndSubdivisionCode(Long userId, String regionCode, String subdivisionCode);

    /** 아직 끝나지 않은(종료일이 date 이후인) 일정 기간 */
    List<FatItineraryPeriod> findByUserIdAndEndDateGreaterThanEqual(Long userId, LocalDate date);
}
