package com.bosshi.maeul.territory.entity;

import com.bosshi.maeul.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * 주인 레벨표의 한 줄. 병아리 레벨표와 같은 구조이며 건국(교환) 레벨은 없다. 관리자 화면에서 조정한다.
 */
@Entity
@Table(
        name = "master_levels",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_master_levels_level", columnNames = {"level"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class MasterLevel extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer level;

    /** 이 레벨에서 다음 레벨까지 필요한 경험치. 최대 레벨에서는 쓰지 않는다. */
    @Column(name = "required_exp", nullable = false)
    private Long requiredExp;

    @Column(name = "stage_name", length = 32)
    private String stageName;
}
