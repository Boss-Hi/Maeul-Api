package com.bosshi.maeul.plan.entity;

import com.bosshi.maeul.common.entity.BaseEntity;
import com.bosshi.maeul.openapi.entity.Tour;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.util.List;

/**
 * AI(Gemini)가 생성한 여행 일정을 나타내는 도메인 모델
 * <p>
 * Itinerary는 Trip에 대한 구체적인 일별 추천 일정을 포함합니다.
 */
@Entity
@Table(name = "itineraries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Itinerary extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 여행 시작일
     */
    @Column(nullable = false)
    @JsonFormat(pattern = "yyyyMMdd")
    private LocalDate startDate;

    /**
     * 여행 종료일
     */
    @Column(nullable = false)
    @JsonFormat(pattern = "yyyyMMdd")
    private LocalDate endDate;

    /**
     * 주축제 (사용자가 선택한 중심 축제)
     */
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "tour_id")
    private Tour tour;

    /**
     * 여행의 일별 일정
     */
    @OneToMany(mappedBy = "itinerary", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItineraryDay> itineraryDays;

    /**
     * 여행의 필터 설정
     */
    /*@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "filter_settings_id")
    private FilterSettings filterSettings;*/
}

