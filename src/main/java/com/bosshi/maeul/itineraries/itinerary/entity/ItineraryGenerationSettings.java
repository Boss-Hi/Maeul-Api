package com.bosshi.maeul.itineraries.itinerary.entity;

import com.bosshi.maeul.common.entity.BaseEntity;
import com.bosshi.maeul.itineraries.itinerary.type.Pace;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalTime;

/**
 * 일정 생성에 적용한 설정. 요청, 설문의 일정 템포, 기본값으로 채워진 뒤의 값이다.
 */
@Entity
@Table(name = "itinerary_generation_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class ItineraryGenerationSettings extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "itinerary_id", nullable = false, unique = true)
    private Itinerary itinerary;

    /**
     * 적용한 페이스
     */
    @Column(name = "pace", nullable = false, length = 16)
    @Enumerated(EnumType.STRING)
    private Pace pace;

    /**
     * 적용한 하루 시작 시간
     */
    @Column(name = "day_start_time", nullable = false)
    private LocalTime dayStartTime;

    /**
     * 적용한 하루 마감 시간
     */
    @Column(name = "day_end_time", nullable = false)
    private LocalTime dayEndTime;
}
