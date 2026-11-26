package com.bosshi.maeul.gacha.entity;

import com.bosshi.maeul.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/** 오프라인 상품 종류. 서버 가챠의 Prize 와 재고가 섞이지 않게 따로 둔다 */
@Entity
@Table(name = "offline_prize_kinds")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class OfflinePrizeKind extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;
}
