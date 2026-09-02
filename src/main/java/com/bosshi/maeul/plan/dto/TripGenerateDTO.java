package com.bosshi.maeul.plan.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripGenerateDTO {
    /**
     * 사용자의 성별
     * 예: "man", "girl"
     */
    @JsonProperty("gender")
    private String gender;

    /**
     * 사용자의 생년월일
     * 예: "1999-01-01"
     */
    @JsonProperty("birthDate")
    private LocalDate birthDate;

    /**
     * 목적지 (도시명)
     * 예: "광주", "부산", "대전"
     */
    @JsonProperty("destination")
    private String destination;

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
        return gender != null &&
                birthDate != null &&
                destination != null &&
                startDate != null &&
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
