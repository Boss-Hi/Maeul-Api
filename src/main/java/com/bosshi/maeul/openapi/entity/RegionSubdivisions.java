package com.bosshi.maeul.openapi.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "region_subdivisions",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_region_subdivision_code", columnNames = {"region_code", "code"})
        }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RegionSubdivisions {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_code", nullable = false)
    private Region region; // 부모 시/도 참조

    @Column(name = "code", nullable = false, length = 16)
    private String code; // 시/군/구 코드 (110, 140 등)

    @Column(name = "name", nullable = false, length = 32)
    private String name; // 시/군/구 명칭 (종로구, 중구 등)

    @Column(name = "rnum")
    private Integer rnum; // 행 번호

    @Builder
    public RegionSubdivisions(String code, String name, Integer rnum) {
        this.code = code;
        this.name = name;
        this.rnum = rnum;
    }

    // 연관관계 편의 메서드용 setter
    protected void setRegion(Region region) {
        this.region = region;
    }
}