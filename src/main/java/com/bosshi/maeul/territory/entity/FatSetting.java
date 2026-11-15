package com.bosshi.maeul.territory.entity;

import com.bosshi.maeul.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * 병아리 설정. 1행만 쓰며 관리자 화면에서 고친다.
 */
@Entity
@Table(name = "fat_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class FatSetting extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 사용자가 동시에 키울 수 있는 병아리 수. 줄여도 이미 키우는 병아리는 그대로 두고 새 분양만 막는다. */
    @Column(name = "max_active_fats", nullable = false)
    private Integer maxActiveFats;

    /** 방문일 하루에 주는 경험치 */
    @Column(name = "visit_day_exp", nullable = false)
    private Integer visitDayExp;
}
