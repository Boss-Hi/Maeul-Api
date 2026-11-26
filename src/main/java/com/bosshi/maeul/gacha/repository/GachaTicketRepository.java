package com.bosshi.maeul.gacha.repository;

import com.bosshi.maeul.gacha.entity.GachaTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface GachaTicketRepository extends JpaRepository<GachaTicket, Long> {
    java.util.Optional<GachaTicket> findByCodeAndDeletedAtIsNull(String code);

    java.util.List<GachaTicket> findByUserIdAndDeletedAtIsNullOrderByIdDesc(Long userId);

    boolean existsByUserIdAndEventTypeAndRefKey(Long userId, com.bosshi.maeul.gacha.TicketEventType eventType, String refKey);

    /** 아직 쓰지 않았고 만료 전이면 사용됨으로 바꾸고 1을, 아니면 0을 돌려준다. 같은 가챠권을 동시에 스캔해도 한 번만 성공한다 */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update GachaTicket t set t.usedAt = :now, t.usedStoreId = :storeId, t.usedByUserId = :ownerUserId "
            + "where t.id = :id and t.usedAt is null and t.expiresAt > :now and t.deletedAt is null")
    int markUsed(@Param("id") Long id, @Param("storeId") Long storeId, @Param("ownerUserId") Long ownerUserId,
                 @Param("now") java.time.LocalDateTime now);

    /** 사용됨이면 사용 전으로 되돌리고 1을 돌려준다 (운영자 취소) */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update GachaTicket t set t.usedAt = null, t.usedStoreId = null, t.usedByUserId = null "
            + "where t.id = :id and t.usedAt is not null")
    int clearUse(@Param("id") Long id);
}
