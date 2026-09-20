package com.bosshi.maeul.itinerary.entity;

import com.bosshi.maeul.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "mission_task_verification_mappings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MissionTaskVerificationMapping extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // MissionTask 과의 N:1 관계
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "mission_task_id", nullable = false)
    private MissionTask missionTask;

    // Cascade 옵션을 추가하여 Mapping이 저장될 때 연결된 MissionTaskVerification도 함께 DB에 저장되도록 설정[cite: 5]
    @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "mission_task_verification_id", nullable = false)
    private MissionTaskVerification missionTaskVerification;

}

