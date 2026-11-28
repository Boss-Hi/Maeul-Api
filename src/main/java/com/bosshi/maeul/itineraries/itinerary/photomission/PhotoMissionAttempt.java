package com.bosshi.maeul.itineraries.itinerary.photomission;

import com.bosshi.maeul.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDate;

/** 사진 미션 시도 기록. 사진은 저장하지 않는다 */
@Entity
@Table(name = "photo_mission_attempts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class PhotoMissionAttempt extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "photo_mission_id", nullable = false)
    private Long photoMissionId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "attempted_on", nullable = false)
    private LocalDate attemptedOn;

    @Column(nullable = false)
    private boolean success;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    @Column(name = "distance_m", nullable = false)
    private double distanceM;

    /** 올린 사진의 SHA-256. 사진 자체는 저장하지 않는다 */
    @Column(name = "image_hash", nullable = false, length = 64)
    private String imageHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AttemptResult result;

    @Column(name = "ai_provider", length = 32)
    private String aiProvider;

    @Column(name = "ai_same")
    private Boolean aiSame;

    @Column(name = "ai_confidence")
    private Double aiConfidence;

    /** 서버 기록용. 기준 사진을 유추하게 하므로 사용자에게 보이지 않는다 */
    @Column(name = "ai_reason", length = 1000)
    private String aiReason;
}
