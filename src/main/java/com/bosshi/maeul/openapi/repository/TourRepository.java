package com.bosshi.maeul.openapi.repository;

import com.bosshi.maeul.openapi.entity.Tour;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.modulith.NamedInterface;

import java.util.List;
import java.util.Optional;

@NamedInterface
public interface TourRepository extends JpaRepository<Tour, String>, JpaSpecificationExecutor<Tour> {
    Optional<Tour> findByContentId(String contentId);

    // 랜덤으로 limit 개수만큼의 Tour 데이터를 조회
    @Query(value = "SELECT * FROM tours WHERE content_type_id = :contentTypeId ORDER BY RAND() LIMIT :limit", nativeQuery = true)
    List<Tour> findRandomToursWhereContentTypeIdIs(@Param("contentTypeId") String contentTypeId, @Param("limit") int limit);
}
