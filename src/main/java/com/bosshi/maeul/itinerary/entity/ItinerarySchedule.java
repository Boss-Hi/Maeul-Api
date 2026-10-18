package com.bosshi.maeul.itinerary.entity;

import com.bosshi.maeul.common.entity.BaseEntity;
import com.bosshi.maeul.itinerary.type.ItineraryScheduleType;
import com.bosshi.maeul.openapi.entity.Tour;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalTime;

/**
 * 일정에 포함된 개별 장소를 나타내는 도메인 모델
 */
@Entity
@Table(name = "itinerary_schedules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class ItinerarySchedule extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "itinerary_day_id", nullable = false)
    private ItineraryDay itineraryDay;

    @Column(name = "type", nullable = false)
    @Builder.Default
    private String type = ItineraryScheduleType.TOUR.name();

    /** TOUR 타입: 한국관광공사 contentId 연동 */
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "content_id")
    private Tour tour;

    /** NORMAL 타입: 직접 입력 제목 */
    @Column(length = 256)
    private String title;

    /** NORMAL 타입: 직접 입력 설명 */
    @Column(columnDefinition = "TEXT")
    private String description;

    /** 순서 (같은 날 내에서 방문 순서) */
    @Column(nullable = false)
    private Integer sequence;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;
}
