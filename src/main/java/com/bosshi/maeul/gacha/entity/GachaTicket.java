package com.bosshi.maeul.gacha.entity;

import com.bosshi.maeul.common.entity.BaseEntity;
import com.bosshi.maeul.gacha.TicketEventType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/** 가챠권: 오프라인 가챠를 돌릴 수 있는 1회용 권. code 가 QR 에 담긴다 */
@Entity
@Table(name = "gacha_tickets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class GachaTicket extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 40)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 32)
    private TicketEventType eventType;

    @Column(name = "ref_key", nullable = false, length = 100)
    private String refKey;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Column(name = "used_store_id")
    private Long usedStoreId;

    @Column(name = "used_by_user_id")
    private Long usedByUserId;
}
