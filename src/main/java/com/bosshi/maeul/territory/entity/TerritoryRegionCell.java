package com.bosshi.maeul.territory.entity;

import com.bosshi.maeul.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * 좌표 격자 1칸이 속한 시군구 (카카오 역지오코딩 캐시). regionId 가 null 이면 지역 밖이다.
 */
@Entity
@Table(name = "territory_region_cells")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class TerritoryRegionCell extends BaseEntity {

    @Id
    @Column(name = "cell_key", length = 32)
    private String cellKey;

    @Column(name = "region_id", length = 10)
    private String regionId;

    @Column(name = "region_name", length = 64)
    private String regionName;

    @Column(name = "parent_region_id", length = 10)
    private String parentRegionId;

    @Column(name = "parent_name", length = 64)
    private String parentName;
}
