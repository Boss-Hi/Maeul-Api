package com.bosshi.maeul.gacha.entity;

import com.bosshi.maeul.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import com.bosshi.maeul.gacha.type.OfflineGachaAction;

/** 사용 이력과 취소 기록 */
@Entity
@Table(name = "offline_gacha_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class OfflineGachaLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private OfflineGachaAction action;

    @Column(name = "ticket_id")
    private Long ticketId;

    @Column(name = "prize_qr_id")
    private Long prizeQrId;

    @Column(name = "store_id")
    private Long storeId;

    /** 스캔한 사장님 또는 취소한 운영자 */
    @Column(name = "actor_user_id", nullable = false)
    private Long actorUserId;

    @Column(length = 500)
    private String reason;
}
