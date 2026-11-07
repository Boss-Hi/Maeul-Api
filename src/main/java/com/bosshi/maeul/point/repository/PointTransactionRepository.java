package com.bosshi.maeul.point.repository;

import com.bosshi.maeul.point.entity.PointTransaction;
import com.bosshi.maeul.point.type.PointReason;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PointTransactionRepository extends JpaRepository<PointTransaction, Long> {

    @Query("select coalesce(sum(p.amount), 0) from PointTransaction p where p.userId = :userId")
    long sumAmountByUserId(@Param("userId") Long userId);

    long countByUserIdAndReason(Long userId, PointReason reason);

    boolean existsByUserIdAndRefKey(Long userId, String refKey);

    List<PointTransaction> findByUserIdOrderByIdDesc(Long userId, Pageable pageable);
}
