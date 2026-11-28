package com.bosshi.maeul.itineraries.itinerary.photomission;

import com.bosshi.maeul.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/** 사진 미션 장소. 운영자가 KraftAdmin 에서 관리한다 */
@Entity
@Table(name = "photo_mission_places")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class PhotoMissionPlace extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 일정 항목 제목에 들어 있어야 하는 장소 키워드 */
    @Column(nullable = false, length = 100)
    private String keyword;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    /** 촬영 허용 반경(미터) */
    @Column(name = "radius_m", nullable = false)
    private int radiusM;

    @Column(nullable = false)
    private boolean enabled;
}
