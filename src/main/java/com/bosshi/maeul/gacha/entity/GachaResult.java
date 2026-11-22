package com.bosshi.maeul.gacha.entity;

import com.bosshi.maeul.common.entity.BaseEntity;
import com.bosshi.maeul.gacha.GachaType;
import com.bosshi.maeul.gacha.RewardType;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/** 뽑힌 가챠 결과. 뽑는 순간의 값을 그대로 남기며, 같은 일(refKey)로는 한 번만 뽑는다 */
@Entity
@Table(name = "gacha_results",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_gacha_results_user_type_ref", columnNames = {"user_id", "gacha_type", "ref_key"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class GachaResult extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "gacha_type", nullable = false, length = 32)
    private GachaType gachaType;

    @Column(name = "ref_key", nullable = false, length = 64)
    private String refKey;

    @Column(name = "outcome_id", nullable = false)
    private Long outcomeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "reward_type", nullable = false, length = 16)
    private RewardType rewardType;

    @Column(nullable = false, length = 100)
    private String label;

    @Column
    private Long amount;

    @Column(name = "prize_id")
    private Long prizeId;

    @Column(name = "item_id")
    private Long itemId;

    /** 사용자가 확인한 시각. null 이면 미확인 */
    @Column(name = "seen_at")
    private LocalDateTime seenAt;
}
