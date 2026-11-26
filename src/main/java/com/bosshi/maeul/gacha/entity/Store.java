package com.bosshi.maeul.gacha.entity;

import com.bosshi.maeul.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/** 매장: 가챠 장비를 둔 오프라인 가게. 운영자가 등록한다 */
@Entity
@Table(name = "stores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Store extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    /** 시도 코드 (region_subdivisions.region_code) */
    @Column(name = "region_code", nullable = false, length = 16)
    private String regionCode;

    /** 시군구 코드 (region_subdivisions.code). 시도 코드와 이어 붙이지 않는다 */
    @Column(name = "subdivision_code", nullable = false, length = 16)
    private String subdivisionCode;
}
