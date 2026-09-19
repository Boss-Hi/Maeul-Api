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

    // 직접 MissionTrigger 를 갖는 대신 중간 매핑 테이블을 참조합니다.[cite: 2]
    @OneToMany(mappedBy = "mission", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MissionTriggerMapping> triggerMappings = new ArrayList<>();

    /**
     * 중간 매핑 테이블(MissionTriggerMapping)을 거쳐
     * 실제 MissionTrigger 목록만 반환하는 편의 메서드
     */
    @Transient // DB 컬럼과 매핑되지 않는 임시/편의용 필드(메서드)임을 명시
    public List<MissionTrigger> getMissionTriggers() {
        return this.triggerMappings.stream()
                .map(MissionTriggerMapping::getMissionTrigger)
                .toList();
    }
}

