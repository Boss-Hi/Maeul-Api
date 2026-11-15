package com.bosshi.maeul.territory.repository;

import com.bosshi.maeul.territory.entity.Fat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface FatRepository extends JpaRepository<Fat, Long> {

    /** 키우는 중인 펫 (분양 순) */
    List<Fat> findByUserIdAndGraduatedAtIsNullOrderByAdoptedAtAsc(Long userId);

    /** 졸업한 펫 (최근 졸업 순) */
    List<Fat> findByUserIdAndGraduatedAtIsNotNullOrderByGraduatedAtDesc(Long userId);

    /** [from, to) 에 분양받아 아직 키우는 중인 펫 */
    List<Fat> findByUserIdAndGraduatedAtIsNullAndAdoptedAtGreaterThanEqualAndAdoptedAtLessThan(
            Long userId, LocalDateTime from, LocalDateTime to);

    long countByUserIdAndGraduatedAtIsNull(Long userId);

    /** 사용자들의 모든 펫 (키우는 중, 졸업) */
    List<Fat> findByUserIdIn(Collection<Long> userIds);

    /** 시군구들에서 키우는 중인 펫 */
    List<Fat> findByGraduatedAtIsNullAndRegionSubdivisionIdIn(Collection<Long> regionSubdivisionIds);

    /** 시군구들에서 키우는 중이거나 졸업한 펫 */
    List<Fat> findByRegionSubdivisionIdIn(Collection<Long> regionSubdivisionIds);

    Optional<Fat> findByIdAndUserId(Long id, Long userId);

    boolean existsByUserIdAndRegionSubdivisionIdAndGraduatedAtIsNull(Long userId, Long regionSubdivisionId);

}
