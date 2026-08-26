package com.bosshi.maeul.plan.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

/**
 * 지역 내 축제 목록 조회 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FestivalListResponse {

    /**
     * 축제 목록
     */
    @JsonProperty("festivals")
    private List<FestivalInfo> festivals;

    /**
     * 전체 축제 개수
     */
    @JsonProperty("totalCount")
    private Integer totalCount;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FestivalInfo {

        /**
         * 한국관광공사 API의 contentId
         */
        @JsonProperty("contentId")
        private String contentId;

        /**
         * 축제명
         */
        @JsonProperty("title")
        private String title;

        /**
         * 축제 개최 시작일
         */
        @JsonProperty("startDate")
        private LocalDate startDate;

        /**
         * 축제 개최 종료일
         */
        @JsonProperty("endDate")
        private LocalDate endDate;

        /**
         * 위도
         */
        @JsonProperty("latitude")
        private Double latitude;

        /**
         * 경도
         */
        @JsonProperty("longitude")
        private Double longitude;

        /**
         * 주소1
         */
        @JsonProperty("address1")
        private String address1;

        /**
         * 주소2
         */
        @JsonProperty("address2")
        private String address2;

        /**
         * 이미지 URL
         */
        @JsonProperty("imageUrl")
        private String imageUrl;

        /**
         * 전화번호
         */
        @JsonProperty("tel")
        private String tel;
    }
}

