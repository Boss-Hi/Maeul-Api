package com.bosshi.maeul.itineraries.itinerary.photomission;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

interface PhotoMissionReferencePhotoRepository extends JpaRepository<PhotoMissionReferencePhoto, Long> {

    /** 이미지 본문(base64)을 읽지 않고 목록만 가져온다 */
    @Query("select new com.bosshi.maeul.itineraries.itinerary.photomission.ReferencePhotoSummary(p.id, p.imageUrl, p.mimeType) "
            + "from PhotoMissionReferencePhoto p where p.placeId = :placeId and p.enabled = true and p.deletedAt is null order by p.id")
    List<ReferencePhotoSummary> findEnabledSummaries(@Param("placeId") Long placeId);

    List<PhotoMissionReferencePhoto> findByPlaceIdAndEnabledTrueAndDeletedAtIsNullOrderByIdAsc(Long placeId);

    boolean existsByCode(String code);
}
