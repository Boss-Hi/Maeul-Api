package com.bosshi.maeul.gacha.entity;

import com.bosshi.maeul.common.entity.BaseEntity;
import com.bosshi.maeul.gacha.GachaType;
import com.bosshi.maeul.gacha.RewardType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/** 가챠 결과 목록의 한 줄. 확률 = weight / 그 가챠에서 뽑을 수 있는 결과의 weight 합 */
@Entity
@Table(name = "gacha_outcomes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class GachaOutcome extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "gacha_type", nullable = false, length = 32)
    private GachaType gachaType;

    @Enumerated(EnumType.STRING)
    @Column(name = "reward_type", nullable = false, length = 16)
    private RewardType rewardType;

    @Column(nullable = false, length = 100)
    private String label;

    @Column(nullable = false)
    private Integer weight;

    @Column
    private Long amount;

    @Column(name = "prize_id")
    private Long prizeId;

    @Column(name = "item_id")
    private Long itemId;
}
