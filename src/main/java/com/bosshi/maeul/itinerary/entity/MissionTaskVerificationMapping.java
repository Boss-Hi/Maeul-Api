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

    // MissionVerification 와의 N:1 관계
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "mission_verification_id", nullable = false)
    private MissionTaskVerification missionVerification;

}

