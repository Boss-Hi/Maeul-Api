package com.bosshi.maeul.timeline.repository;

import com.bosshi.maeul.timeline.entity.TimelinePoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TimelinePointRepository extends JpaRepository<TimelinePoint, Long> {

    /** [from, to) 구간의 위치 기록을 시간순으로 조회 */
    List<TimelinePoint> findByUserIdAndRecordedAtGreaterThanEqualAndRecordedAtLessThanOrderByRecordedAtAsc(
            Long userId, LocalDateTime from, LocalDateTime to);

    /** [from, to) 구간의 위치 기록 삭제 */
    @Modifying(clearAutomatically = true)
    @Query("delete from TimelinePoint p where p.userId = :userId and p.recordedAt >= :from and p.recordedAt < :to")
    int deleteByUserIdAndRange(@Param("userId") Long userId, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
