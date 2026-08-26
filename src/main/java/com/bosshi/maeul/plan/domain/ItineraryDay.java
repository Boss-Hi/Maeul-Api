package com.bosshi.maeul.plan.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 일정의 특정 하루를 나타내는 도메인 모델
 *
 * ItineraryDay는 특정 날짜의 테마와 방문할 장소들을 포함합니다.
 */
@Entity
@Table(name = "itinerary_days")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItineraryDay {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    /**
     * 연결된 Itinerary
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "itinerary_id", nullable = false)
    private Itinerary itinerary;

    /**
     * 해당 날짜
     */
    @Column(nullable = false)
    private LocalDate date;

    /**
     * 일의 순서 (1부터 시작)
     */
    @Column(nullable = false)
    private Integer dayNumber;

    /**
     * 해당 일의 테마
     * 예: "축제 중심", "체험 중심", "문화유산 투어"
     */
    @Column(length = 100)
    private String theme;

    /**
     * 해당 일의 추천 장소들
     */
    @OneToMany(mappedBy = "itineraryDay", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @OrderBy("sequence ASC")
    private List<ItineraryVenue> venues = new ArrayList<>();

    /**
     * 생성 시간
     */
    @Column(nullable = false, updatable = false)
    private java.time.LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = java.time.LocalDateTime.now();
    }

    /**
     * 해당 일의 장소 수를 반환합니다.
     */
    public Integer getVenueCount() {
        return venues != null ? venues.size() : 0;
    }
}

