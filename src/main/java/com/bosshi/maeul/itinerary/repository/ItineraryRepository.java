package com.bosshi.maeul.itinerary.repository;

import com.bosshi.maeul.itinerary.entity.Itinerary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface ItineraryRepository extends JpaRepository<Itinerary, Long> {
    // endDate가 현재 날짜(currentDate)보다 크거나 같은 일정 중에서
    // 시작일이 가장 최근인 일정 하나를 가져옵니다.
    // FETCH JOIN을 통해 매핑 테이블과 실제 미션 정보를 한 번에 로딩
    @Query("SELECT i FROM Itinerary i " +
            "LEFT JOIN FETCH i.itineraryMissionMappings imm " +
            "LEFT JOIN FETCH imm.mission m " +
            "WHERE i.userId = :userId AND i.endDate >= :currentDate " +
            "ORDER BY i.createdAt DESC LIMIT 1")
    Optional<Itinerary> findFirstByUserIdAndEndDateGreaterThanEqualOrderByStartDateDesc(
            @Param("userId") Long userId,
            @Param("currentDate") LocalDate currentDate
    );
}

