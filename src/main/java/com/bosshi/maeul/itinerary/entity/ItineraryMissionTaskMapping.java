package com.bosshi.maeul.itinerary.entity;

import com.bosshi.maeul.common.entity.BaseEntity;
import com.bosshi.maeul.itinerary.type.MissionStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "itinerary_mission_task_mappings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItineraryMissionTaskMapping extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "status", nullable = false, length = 16)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private MissionStatus status = MissionStatus.READY;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "itinerary_mission_mapping_id", nullable = false)
    private ItineraryMissionMapping itineraryMissionMapping;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_task_id", nullable = false)
    private MissionTask missionTask;
}

