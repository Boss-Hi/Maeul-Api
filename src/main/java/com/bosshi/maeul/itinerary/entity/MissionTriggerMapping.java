package com.bosshi.maeul.itinerary.entity;

import com.bosshi.maeul.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "mission_trigger_mappings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MissionTriggerMapping extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Mission 과의 N:1 관계
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "mission_task_id", nullable = false)
    private MissionTask missionTask;

    // MissionTrigger 와의 N:1 관계
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "mission_trigger_id", nullable = false)
    private MissionTrigger missionTrigger;

}

