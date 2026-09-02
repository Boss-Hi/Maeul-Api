package com.bosshi.maeul.itinerary.dto;

import com.bosshi.maeul.openapi.entity.Tour;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItineraryGenerateDTO {
    @JsonProperty("tour_id")
    private String tourId;

    @JsonProperty("tour")
    private Tour tour;

    @JsonProperty("recommendableTours")
    private List<Tour> recommendableTours;

    /**
     * 여행 시작일
     * 예: "2026-09-01"
     */
    @JsonProperty("startDate")
    private LocalDate startDate;

    /**
     * 여행 종료일
     * 예: "2026-09-10"
     */
    @JsonProperty("endDate")
    private LocalDate endDate;

    /**
     * 선택한 카테고리 목록
     * 예: ["축제/공연", "체험관광/공예", "숙박/호텔"]
     */
    @JsonProperty("selectedCategories")
    private List<String> selectedCategories;

    /**
     * 필터 설정 (선택사항)
     */
    @JsonProperty("filterSettings")
    private FilterSettingsRequest filterSettings;

    /**
     * 입력 값 검증
     */
    public boolean isValid() {
        return startDate != null &&
                endDate != null &&
                selectedCategories != null &&
                !selectedCategories.isEmpty() &&
                endDate.isAfter(startDate);
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FilterSettingsRequest {

        /**
         * 하루에 최대 추천 장소 수
         */
        @JsonProperty("maxVenuesPerDay")
        private Integer maxVenuesPerDay;

        /**
         * MainFestival 주변 반경 (단위: km)
         */
        @JsonProperty("maxDistanceKm")
        private Integer maxDistanceKm;

        /**
         * 하루에 여러 카테고리 혼합 여부
         */
        @JsonProperty("allowCategoryMixPerDay")
        private Boolean allowCategoryMixPerDay;
    }
}
