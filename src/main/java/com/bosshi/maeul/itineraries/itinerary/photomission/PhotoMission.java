package com.bosshi.maeul.itineraries.itinerary.photomission;

import com.bosshi.maeul.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** 일정에 붙은 사진 미션 */
@Entity
@Table(name = "photo_missions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class PhotoMission extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "itinerary_id", nullable = false)
    private Long itineraryId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "place_id", nullable = false)
    private Long placeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    @Builder.Default
    private PhotoMissionStatus status = PhotoMissionStatus.READY;

    /** 일정 주축제의 시도 코드 (region_subdivisions.region_code) */
    @Column(name = "region_code", length = 16)
    private String regionCode;

    /** 일정 주축제의 시군구 코드 (region_subdivisions.code). 시도 코드와 이어 붙이지 않는다 */
    @Column(name = "subdivision_code", length = 16)
    private String subdivisionCode;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /** 가챠권을 발급했는가 */
    @Column(nullable = false)
    private boolean rewarded;
}
