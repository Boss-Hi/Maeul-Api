package com.bosshi.maeul.point.entity;

import com.bosshi.maeul.common.entity.BaseEntity;
import com.bosshi.maeul.point.type.PointReason;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * 포인트 적립/사용 1건. 잔액은 amount 의 합이다.
 */
@Entity
@Table(
        name = "point_transactions",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_point_transactions_user_ref", columnNames = {"user_id", "ref_key"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class PointTransaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PointReason reason;

    /** 같은 지급을 두 번 하지 않기 위한 키 (예: OCCUPATION:3 = 점령 보상 3번째) */
    @Column(name = "ref_key", nullable = false, length = 64)
    private String refKey;
}
