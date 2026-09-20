package com.bosshi.maeul.itinerary.entity;

import com.bosshi.maeul.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "mission_tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MissionTask extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 256)
    private String name;

    @Column(nullable = false, length = 2048)
    private String description;

    @OneToMany(mappedBy = "missionTask", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MissionTaskVerificationMapping> missionTaskVerificationMappings = new ArrayList<>();

    @Transient // DB 컬럼과 매핑되지 않는 임시/편의용 필드(메서드)임을 명시
    public List<MissionTaskVerification> getMissionTaskVerifications() {
        return this.missionTaskVerificationMappings.stream()
                .map(MissionTaskVerificationMapping::getMissionTaskVerification)
                .toList();
    }
}
