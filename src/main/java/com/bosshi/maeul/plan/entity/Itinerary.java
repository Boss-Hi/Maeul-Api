package com.bosshi.maeul.plan.entity;

import com.bosshi.maeul.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.ArrayList;
import java.util.List;

/**
 * AI(Gemini)가 생성한 여행 일정을 나타내는 도메인 모델
 *
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

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 연결된 Trip
     */
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "trip_id", nullable = false, unique = true)
    private Trip trip;

    /**
     * 여행 전체 요약
     */
    @Column(length = 1000)
    private String summary;

    // cascade = CascadeType.ALL 또는 PERSIST 추가
    @OneToMany(mappedBy = "itinerary", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItineraryDay> days = new ArrayList<>();

    /**
     * 일정 일수를 반환합니다.
     */
    public Integer getDayCount() {
        return days != null ? days.size() : 0;
    }
}

