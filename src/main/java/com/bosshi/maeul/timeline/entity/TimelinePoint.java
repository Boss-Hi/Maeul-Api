package com.bosshi.maeul.timeline.entity;

import com.bosshi.maeul.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 사용자 기기에서 수집한 위치 기록 1건
 */
@Entity
@Table(
        name = "timeline_points",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_timeline_points_user_recorded_at", columnNames = {"user_id", "recorded_at"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class TimelinePoint extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    /** 기기가 알려준 정확도 (m) */
    private Double accuracy;

    /** 기기에서 위치를 수집한 시각 (timeline.zone 기준) */
    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;
}
