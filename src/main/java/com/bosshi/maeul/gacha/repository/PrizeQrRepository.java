package com.bosshi.maeul.gacha.repository;

import com.bosshi.maeul.gacha.entity.PrizeQr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface PrizeQrRepository extends JpaRepository<PrizeQr, Long> {
    java.util.Optional<PrizeQr> findByCodeAndDeletedAtIsNull(String code);

    boolean existsByTicketId(Long ticketId);

    java.util.List<PrizeQr> findByTicketIdIn(java.util.Collection<Long> ticketIds);

    java.util.Optional<PrizeQr> findByTicketId(Long ticketId);

    org.springframework.data.domain.Page<PrizeQr> findByKindIdAndDeletedAtIsNullOrderByIdAsc(Long kindId, org.springframework.data.domain.Pageable pageable);

    java.util.List<PrizeQr> findByKindIdAndDeletedAtIsNullOrderByIdAsc(Long kindId);

    long countByKindIdAndDeletedAtIsNull(Long kindId);

    long countByKindIdAndDeliveredAtIsNotNullAndDeletedAtIsNull(Long kindId);

    /** 아직 지급되지 않았으면 지급 완료로 바꾸고 가챠권과 연결해 1을, 아니면 0을 돌려준다. 같은 상품 QR 을 동시에 스캔해도 한 번만 성공한다 */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update PrizeQr q set q.deliveredAt = :now, q.deliveredStoreId = :storeId, q.deliveredByUserId = :ownerUserId, q.ticketId = :ticketId "
            + "where q.id = :id and q.deliveredAt is null and q.deletedAt is null")
    int markDelivered(@Param("id") Long id, @Param("storeId") Long storeId, @Param("ownerUserId") Long ownerUserId,
                      @Param("ticketId") Long ticketId, @Param("now") java.time.LocalDateTime now);

    /** 지급 완료면 미지급으로 되돌리고 가챠권 연결을 끊어 1을 돌려준다 (운영자 취소) */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update PrizeQr q set q.deliveredAt = null, q.deliveredStoreId = null, q.deliveredByUserId = null, q.ticketId = null "
            + "where q.id = :id and q.deliveredAt is not null")
    int clearDelivery(@Param("id") Long id);
}
