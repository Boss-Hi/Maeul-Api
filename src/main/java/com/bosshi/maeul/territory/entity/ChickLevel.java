package com.bosshi.maeul.territory.entity;

import com.bosshi.maeul.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * 병아리 레벨표의 한 줄. 관리자 화면에서 조정한다.
 */
@Entity
@Table(
        name = "chick_levels",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_chick_levels_level", columnNames = {"level"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class ChickLevel extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer level;

    /** 이 레벨에서 다음 레벨까지 필요한 경험치. 최대 레벨에서는 쓰지 않는다. */
    @Column(name = "required_exp", nullable = false)
    private Long requiredExp;

    /** 단계 이름. 다음 단계 이름이 붙은 레벨 전까지 표시한다. */
    @Column(name = "stage_name", length = 32)
    private String stageName;

    /** 건국(상품 교환 가능) 레벨 여부. 여러 줄이 켜져 있으면 가장 낮은 레벨을 쓴다. */
    @Column(nullable = false)
    private Boolean founding;
}
