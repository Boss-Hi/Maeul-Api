package com.bosshi.maeul.gacha.repository;

import com.bosshi.maeul.gacha.entity.Prize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface PrizeRepository extends JpaRepository<Prize, Long> {

    List<Prize> findByIdInAndDeletedAtIsNull(Collection<Long> ids);

    /** 재고가 남아 있으면 1 줄이고 1을, 없으면 0을 돌려준다 */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Prize p set p.stock = p.stock - 1 where p.id = :id and p.stock > 0 and p.deletedAt is null")
    int takeOne(@Param("id") Long id);
}
