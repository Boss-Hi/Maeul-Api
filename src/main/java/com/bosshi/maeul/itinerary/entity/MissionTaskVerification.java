package com.bosshi.maeul.itinerary.entity;

import com.bosshi.maeul.common.entity.BaseEntity;
import com.bosshi.maeul.itinerary.type.MissionVerificationType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "mission_task_verifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MissionTaskVerification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 32)
    @Enumerated(EnumType.STRING)
    private MissionVerificationType type;

    @Column(nullable = false, length = 32)
    private String name;

    @Column(nullable = false, length = 256)
    private String value;

    @Column(nullable = false, length = 2048)
    private String description;

}

