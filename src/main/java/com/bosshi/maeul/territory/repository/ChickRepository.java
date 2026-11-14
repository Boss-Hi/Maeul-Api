package com.bosshi.maeul.territory.repository;

import com.bosshi.maeul.territory.entity.Chick;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChickRepository extends JpaRepository<Chick, Long> {

    /** 키우는 중인 병아리 (분양 순) */
    List<Chick> findByUserIdAndGraduatedAtIsNullOrderByAdoptedAtAsc(Long userId);

    /** 졸업한 병아리 (최근 졸업 순) */
    List<Chick> findByUserIdAndGraduatedAtIsNotNullOrderByGraduatedAtDesc(Long userId);

    /** [from, to) 에 분양받아 아직 키우는 중인 병아리 */
    List<Chick> findByUserIdAndGraduatedAtIsNullAndAdoptedAtGreaterThanEqualAndAdoptedAtLessThan(
            Long userId, LocalDateTime from, LocalDateTime to);

    Optional<Chick> findByIdAndUserId(Long id, Long userId);

    boolean existsByUserIdAndRegionIdAndGraduatedAtIsNull(Long userId, String regionId);

    boolean existsByUserIdAndRegionIdAndGraduatedAtGreaterThanEqual(Long userId, String regionId, LocalDateTime from);
}
