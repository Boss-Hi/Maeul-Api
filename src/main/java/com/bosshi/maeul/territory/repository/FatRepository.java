package com.bosshi.maeul.territory.repository;

import com.bosshi.maeul.territory.entity.Fat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FatRepository extends JpaRepository<Fat, Long> {

    /** 키우는 중인 병아리 (분양 순) */
    List<Fat> findByUserIdAndGraduatedAtIsNullOrderByAdoptedAtAsc(Long userId);

    /** 졸업한 병아리 (최근 졸업 순) */
    List<Fat> findByUserIdAndGraduatedAtIsNotNullOrderByGraduatedAtDesc(Long userId);

    /** [from, to) 에 분양받아 아직 키우는 중인 병아리 */
    List<Fat> findByUserIdAndGraduatedAtIsNullAndAdoptedAtGreaterThanEqualAndAdoptedAtLessThan(
            Long userId, LocalDateTime from, LocalDateTime to);

    Optional<Fat> findByIdAndUserId(Long id, Long userId);

    boolean existsByUserIdAndRegionIdAndGraduatedAtIsNull(Long userId, String regionId);

    boolean existsByUserIdAndRegionIdAndGraduatedAtGreaterThanEqual(Long userId, String regionId, LocalDateTime from);
}
