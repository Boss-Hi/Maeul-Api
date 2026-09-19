package com.bosshi.maeul.itinerary.entity;

import com.bosshi.maeul.common.entity.BaseEntity;
import com.bosshi.maeul.itinerary.type.MissionVerificationType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "mission_verifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MissionVerification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 32)
    @Enumerated(EnumType.STRING)
    private MissionVerificationType type;

    @Column(nullable = false, length = 256)
    private String value;

    @Column(nullable = false, length = 2048)
    private String description;

}

