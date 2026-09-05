package com.bosshi.maeul.openapi.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "l_dong_regions")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LDongRegion {

    @Id
    @Column(name = "code", length = 16)
    private String code; // 시/도 코드 (PK)

    @Column(name = "name", nullable = false, length = 32)
    private String name; // 시/도 명칭

    @OneToMany(mappedBy = "region", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LDongSigungu> sigungus = new ArrayList<>();

    @Builder
    public LDongRegion(String code, String name) {
        this.code = code;
        this.name = name;
    }

    // 연관관계 편의 메서드
    public void addSigungu(LDongSigungu sigungu) {
        this.sigungus.add(sigungu);
        sigungu.setRegion(this);
    }
}