package com.bosshi.maeul.gacha.entity;

import com.bosshi.maeul.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

/** 상품 QR: 장비에서 나온 캡슐에 들어 있는 고유 1회용 코드. 사장님이 스캔하면 지급 완료 */
@Entity
@Table(name = "prize_qrs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class PrizeQr extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "kind_id", nullable = false)
    private Long kindId;

    @Column(nullable = false, length = 40)
    private String code;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "delivered_store_id")
    private Long deliveredStoreId;

    @Column(name = "delivered_by_user_id")
    private Long deliveredByUserId;

    /** 연결된 가챠권. 가챠권 한 장에 상품 QR 은 하나만 연결된다 */
    @Column(name = "ticket_id")
    private Long ticketId;
}
