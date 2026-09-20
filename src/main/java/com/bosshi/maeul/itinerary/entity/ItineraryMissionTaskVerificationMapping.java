package com.bosshi.maeul.itinerary.entity;

import com.bosshi.maeul.common.entity.BaseEntity;
import com.bosshi.maeul.itinerary.type.MissionVerificationType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "itinerary_mission_task_verification_mappings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItineraryMissionTaskVerificationMapping extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 256)
    private String name;

    @Column(nullable = false, length = 2048)
    private String description;

    @Column(name = "type", nullable = false, length = 16)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private MissionVerificationType type = MissionVerificationType.GPS;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "itinerary_mission_task_mapping_id", nullable = false)
    private ItineraryMissionTaskMapping itineraryMissionTaskMapping;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_task_verification_id", nullable = false)
    private MissionTaskVerification missionTaskVerification;
}

