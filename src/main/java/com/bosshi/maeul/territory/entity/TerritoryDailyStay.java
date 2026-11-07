package com.bosshi.maeul.territory.entity;

import com.bosshi.maeul.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;

/**
 * 사용자가 하루 동안 한 시군구에 있었던 시간 (점령 시간)
 */
@Entity
@Table(
        name = "territory_daily_stays",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_territory_daily_stays", columnNames = {"user_id", "region_id", "stay_date"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class TerritoryDailyStay extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "region_id", nullable = false, length = 10)
    private String regionId;

    @Column(name = "stay_date", nullable = false)
    private LocalDate stayDate;

    @Column(name = "stay_seconds", nullable = false)
    private Long staySeconds;
}
