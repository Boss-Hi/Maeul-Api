package com.bosshi.maeul.territory.entity;

import com.bosshi.maeul.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;

/**
 * 병아리가 자라는 일정 기간 1개 (일정 모듈 이벤트로 채우는 복사본, ADR 0013).
 * 시군구는 일정 주축제의 시도 코드와 시군구 코드이고, 기간은 시작일 0시부터 종료일 24시까지다.
 */
@Entity
@Table(
        name = "fat_itinerary_periods",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_fat_itinerary_periods_itinerary", columnNames = {"itinerary_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class FatItineraryPeriod extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "itinerary_id", nullable = false)
    private Long itineraryId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "region_code", nullable = false, length = 16)
    private String regionCode;

    @Column(name = "subdivision_code", nullable = false, length = 16)
    private String subdivisionCode;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;
}
