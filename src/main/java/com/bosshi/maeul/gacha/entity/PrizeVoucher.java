package com.bosshi.maeul.gacha.entity;

import com.bosshi.maeul.common.entity.BaseEntity;
import com.bosshi.maeul.gacha.type.VoucherStatus;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/** 교환권: 가챠에서 상품이 나오면 발급한다. 지급 완료는 운영자가 관리자 화면에서 정한다 */
@Entity
@Table(name = "prize_vouchers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class PrizeVoucher extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "prize_id", nullable = false)
    private Long prizeId;

    @Column(name = "gacha_result_id", nullable = false)
    private Long gachaResultId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private VoucherStatus status;

    @Column(name = "fulfilled_at")
    private LocalDateTime fulfilledAt;

    /** 운영자가 관리자 화면에서 지급 완료로 바꾸면 그 시각을 남긴다 */
    @Override
    @PreUpdate
    protected void preUpdate() {
        if (status == VoucherStatus.FULFILLED && fulfilledAt == null) {
            fulfilledAt = now();
        }
    }
}
