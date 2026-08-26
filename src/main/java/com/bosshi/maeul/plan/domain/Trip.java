package com.bosshi.maeul.plan.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 여행 정보를 나타내는 도메인 모델
 *
 * Trip은 사용자가 계획하는 특정 지역, 기간의 여행을 표현합니다.
 */
@Entity
@Table(name = "trips")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    /**
     * 사용자의 성별 (man, girl)
     */
    @Column(nullable = false)
    private String gender;

    /**
     * 사용자의 생년월일
     */
    @Column(nullable = false)
    private LocalDate birthDate;

    /**
     * 목적지 (광주, 부산, 대전 등)
     */
    @Column(nullable = false)
    private String destination;

    /**
     * 여행 시작일
     */
    @Column(nullable = false)
    private LocalDate startDate;

    /**
     * 여행 종료일
     */
    @Column(nullable = false)
    private LocalDate endDate;

    /**
     * 사용자가 선택한 카테고리 (쉼표로 구분)
     * 예: "축제/공연,체험관광/공예,숙박/호텔"
     */
    @Column(nullable = false, length = 500)
    private String selectedCategories;

    /**
     * 주축제 (사용자가 선택한 중심 축제)
     */
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "main_festival_id")
    private MainFestival mainFestival;

    /**
     * 여행의 필터 설정
     */
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "filter_settings_id")
    private FilterSettings filterSettings;

    /**
     * 생성된 일정
     */
    @OneToOne(mappedBy = "trip", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Itinerary itinerary;

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

    /**
     * 선택한 카테고리 목록을 반환합니다.
     */
    public List<String> getSelectedCategoriesList() {
        if (selectedCategories == null || selectedCategories.isEmpty()) {
            return new ArrayList<>();
        }
        return List.of(selectedCategories.split(","));
    }

    /**
     * 여행 기간(일 수)을 반환합니다.
     */
    public long getDurationDays() {
        return java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }
}

