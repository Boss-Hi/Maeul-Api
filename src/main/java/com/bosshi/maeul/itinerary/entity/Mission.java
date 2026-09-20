package com.bosshi.maeul.itinerary.entity;

import com.bosshi.maeul.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "missions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mission extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 32)
    private String code;

    @Column(nullable = false, length = 256)
    private String name;

    @Column(nullable = false, length = 2048)
    private String description;

    @OneToMany(mappedBy = "mission", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MissionTaskMapping> missionTaskMappings = new ArrayList<>();

    @OneToMany(mappedBy = "mission", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MissionTriggerMapping> missionTriggerMappings = new ArrayList<>();

    @Transient
    public List<MissionTask> getMissionTasks() {
        return this.missionTaskMappings.stream()
                .map(MissionTaskMapping::getMissionTask)
                .toList();
    }

    @Transient
    public List<MissionTrigger> getMissionTriggers() {
        return this.missionTriggerMappings.stream()
                .map(MissionTriggerMapping::getMissionTrigger)
                .toList();
    }
}

