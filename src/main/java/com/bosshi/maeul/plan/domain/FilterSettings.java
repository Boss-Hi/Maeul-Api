package com.bosshi.maeul.plan.domain;

import jakarta.persistence.*;
import lombok.*;

/**
 * 여행 일정 생성 시 적용되는 필터 조건을 나타내는 도메인 모델
 *
 * 관리자가 설정한 글로벌 기본값과 사용자의 오버라이드 값을 포함합니다.
 */
@Entity
@Table(name = "filter_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FilterSettings extends com.bosshi.maeul.common.domain.BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    /**
     * 하루에 최대 추천 장소 수
     * 기본값: 5
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer maxVenuesPerDay = 5;

    /**
     * MainFestival 주변 반경 (단위: km)
     * 기본값: 10
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer maxDistanceKm = 10;

    /**
     * 하루에 여러 카테고리 혼합 여부
     * 기본값: true
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean allowCategoryMixPerDay = true;

    /**
     * 글로벌 설정 여부 (true면 관리자 설정 사용, false면 사용자 오버라이드)
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean isGlobal = true;

}

