package com.bosshi.maeul.plan.entity;

import com.bosshi.maeul.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * 일정에 포함된 개별 장소를 나타내는 도메인 모델
 * <p>
 * ItineraryTour는 ItineraryDay에 속한 관광지, 축제, 식당, 숙박시설 등을 나타냅니다.
 */
@Entity
@Table(name = "itinerary_tours")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class ItineraryTour extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 연결된 ItineraryDay
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "itinerary_day_id", nullable = false)
    private ItineraryDay itineraryDay;

    /**
     * 한국관광공사 API의 contentId
     */
    @Column(nullable = false)
    private String contentId;

    /**
     * 순서 (같은 날 내에서 방문 순서)
     */
    @Column(nullable = false)
    private Integer sequence;

}

