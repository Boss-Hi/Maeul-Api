package com.bosshi.maeul.territory.entity;

import com.bosshi.maeul.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 사용자가 시군구 하나에서 점령 시간으로 키우는 병아리.
 * 경험치는 저장하지 않고 분양 이후 점령 시간에서 계산한다 (ADR 0010). 졸업 때의 값만 남긴다.
 */
@Entity
@Table(name = "chicks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Chick extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "region_id", nullable = false, length = 10)
    private String regionId;

    @Column(name = "adopted_at", nullable = false)
    private LocalDateTime adoptedAt;

    /** 분양 당일 분양 시각 이후 점령 시간. 그날 위치 기록이 바뀔 때마다 다시 계산한다. */
    @Column(name = "adoption_day_seconds", nullable = false)
    private Long adoptionDaySeconds;

    @Column(name = "graduated_at")
    private LocalDateTime graduatedAt;

    @Column(name = "graduated_level")
    private Integer graduatedLevel;

    @Column(name = "graduated_exp")
    private Long graduatedExp;

    public boolean isGraduated() {
        return graduatedAt != null;
    }

    public void graduate(LocalDateTime at, int level, long exp) {
        this.graduatedAt = at;
        this.graduatedLevel = level;
        this.graduatedExp = exp;
    }
}
