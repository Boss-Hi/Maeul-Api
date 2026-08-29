package com.bosshi.maeul.plan.entity;

import com.bosshi.maeul.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 일정에 포함된 개별 장소를 나타내는 도메인 모델
 *
 * ItineraryVenue는 ItineraryDay에 속한 관광지, 축제, 식당, 숙박시설 등을 나타냅니다.
 */
@Entity
@Table(name = "itinerary_venues")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class ItineraryVenue extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

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
     * 장소명
     */
    @Column(nullable = false)
    private String name;

    /**
     * 카테고리
     * 예: "축제/공연", "체험관광/공예", "숙박/호텔", "음식"
     */
    @Column(nullable = false)
    private String category;

    /**
     * 방문 시간
     * 예: "14:00-18:00" 또는 "09:00-12:00"
     */
    @Column(length = 50)
    private String visitTime;

    /**
     * 권장 체류 시간
     * 예: "2시간", "3시간 30분"
     */
    @Column(length = 50)
    private String duration;

    /**
     * 위치 설명 또는 추가 정보
     */
    @Column(length = 500)
    private String description;

    /**
     * 위도
     */
    @Column
    private Double latitude;

    /**
     * 경도
     */
    @Column
    private Double longitude;

    /**
     * 순서 (같은 날 내에서 방문 순서)
     */
    @Column(nullable = false)
    private Integer sequence;

        }

