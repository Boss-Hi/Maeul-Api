package com.bosshi.maeul.territory.entity;

import com.bosshi.maeul.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * 미션 가챠로 펫이 얻은 경험치 1건 (ADR 0014). 같은 미션은 한 번만 주고, 위치 기록을 지워도 회수하지 않는다.
 */
@Entity
@Table(
        name = "fat_mission_exps",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_fat_mission_exps_user_mission", columnNames = {"user_id", "itinerary_mission_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class FatMissionExp extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "fat_id", nullable = false)
    private Long fatId;

    @Column(name = "itinerary_mission_id", nullable = false)
    private Long itineraryMissionId;

    @Column(name = "gacha_result_id", nullable = false)
    private Long gachaResultId;

    @Column(nullable = false)
    private Long exp;
}
