package com.bosshi.maeul.territory.entity;

import com.bosshi.maeul.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * 완료된 일정 1개 (ADR 0014). 같은 주축제 시군구에 기간이 겹치는 일정들은 묶어서 한 번만 완료하며, 묶음의 일정마다 1행을 남긴다.
 */
@Entity
@Table(
        name = "itinerary_completions",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_itinerary_completions_itinerary", columnNames = {"itinerary_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class ItineraryCompletion extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "itinerary_id", nullable = false)
    private Long itineraryId;

    @Column(name = "gacha_result_id", nullable = false)
    private Long gachaResultId;
}
