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

    // mission_task_mappings 테이블: mission_id + mission_task_id (Mission 수준의 태스크 매핑)
    @OneToMany(mappedBy = "mission", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MissionTaskMapping> missionTaskMappings = new ArrayList<>();

    @Transient // DB 컬럼과 매핑되지 않는 임시/편의용 필드(메서드)임을 명시
    public List<MissionTask> getMissionTasks() {
        return this.missionTaskMappings.stream()
                .map(MissionTaskMapping::getMissionTask)
                .toList();
    }
}

