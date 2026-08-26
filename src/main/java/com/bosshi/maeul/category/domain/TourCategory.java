package com.bosshi.maeul.category.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.modulith.NamedInterface;

@NamedInterface
@Entity
@Table(name = "tour_categories")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    /**
     * 카테고리 코드
     * 예: "AC" (숙박), "EV" (축제/공연), "EX" (체험관광), "FD" (음식)
     */
    @Column(nullable = false, unique = true, length = 10)
    private String code;

    /**
     * 카테고리명
     * 예: "축제/공연", "체험관광/공예", "숙박/호텔", "음식"
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * 한국관광공사 API의 contenttypeid
     * 예: "15" (축제/공연), "12" (체험관광), "32" (숙박), "39" (음식)
     */
    @Column(nullable = false, length = 10)
    private String contentTypeId;

    /**
     * 카테고리 설명
     */
    @Column(length = 500)
    private String description;

    /**
     * 사용 여부
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    /**
     * 생성 시간
     */
    @Column(nullable = false, updatable = false)
    private java.time.LocalDateTime createdAt;

    /**
     * 수정 시간
     */
    @Column(nullable = false)
    private java.time.LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = java.time.LocalDateTime.now();
        this.updatedAt = java.time.LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = java.time.LocalDateTime.now();
    }
}
