package com.bosshi.maeul.itineraries.itinerary.photomission;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

interface PhotoMissionRepository extends JpaRepository<PhotoMission, Long> {

    boolean existsByItineraryIdAndPlaceId(Long itineraryId, Long placeId);

    List<PhotoMission> findByItineraryIdInAndDeletedAtIsNull(java.util.Collection<Long> itineraryIds);

    /** 같은 사용자, 같은 시군구에 기간이 겹치고 이미 가챠권을 받은 사진 미션이 있는가 */
    @Query("select count(m) > 0 from PhotoMission m where m.userId = :userId and m.regionCode = :regionCode "
            + "and m.subdivisionCode = :subdivisionCode and m.rewarded = true and m.id <> :excludeId "
            + "and m.startDate <= :endDate and m.endDate >= :startDate and m.deletedAt is null")
    boolean existsRewardedOverlap(@Param("userId") Long userId, @Param("regionCode") String regionCode,
                                  @Param("subdivisionCode") String subdivisionCode, @Param("startDate") LocalDate startDate,
                                  @Param("endDate") LocalDate endDate, @Param("excludeId") Long excludeId);

    /** 아직 완료되지 않았으면 완료로 바꾸고 1을 돌려준다. 동시에 성공해도 한 번만 */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update PhotoMission m set m.status = com.bosshi.maeul.itineraries.itinerary.photomission.PhotoMissionStatus.COMPLETE, "
            + "m.completedAt = :now, m.rewarded = :rewarded where m.id = :id "
            + "and m.status = com.bosshi.maeul.itineraries.itinerary.photomission.PhotoMissionStatus.READY")
    int markComplete(@Param("id") Long id, @Param("now") LocalDateTime now, @Param("rewarded") boolean rewarded);
}
