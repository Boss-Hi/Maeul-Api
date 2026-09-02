package com.bosshi.maeul.itinerary.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

/**
 * 생성된 여행 일정 응답 DTO
 *
 * AI(Gemini)가 생성한 일별 추천 일정을 클라이언트에 반환합니다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItineraryResponse {

    /**
     * 일정의 고유 ID
     */
    @JsonProperty("tripId")
    private String tripId;

    /**
     * 목적지
     */
    @JsonProperty("destination")
    private String destination;

    /**
     * 여행 시작일
     */
    @JsonProperty("startDate")
    private LocalDate startDate;

    /**
     * 여행 종료일
     */
    @JsonProperty("endDate")
    private LocalDate endDate;

    /**
     * 주축제 정보
     */
    @JsonProperty("mainFestival")
    private MainFestivalInfo mainFestival;

    /**
     * 여행 전체 요약
     */
    @JsonProperty("summary")
    private String summary;

    /**
     * 일별 일정 리스트
     */
    @JsonProperty("days")
    private List<ItineraryDayResponse> days;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MainFestivalInfo {

        @JsonProperty("name")
        private String name;

        @JsonProperty("category")
        private String category;

        @JsonProperty("startDate")
        private LocalDate startDate;

        @JsonProperty("endDate")
        private LocalDate endDate;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ItineraryDayResponse {

        /**
         * 해당 날짜
         */
        @JsonProperty("date")
        private LocalDate date;

        /**
         * 일의 순서 (1부터 시작)
         */
        @JsonProperty("dayNumber")
        private Integer dayNumber;

        /**
         * 해당 일의 테마
         */
        @JsonProperty("theme")
        private String theme;

        /**
         * 해당 일의 추천 장소들
         */
        @JsonProperty("venues")
        private List<ItineraryVenueResponse> venues;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ItineraryVenueResponse {

        /**
         * 장소명
         */
        @JsonProperty("name")
        private String name;

        /**
         * 카테고리
         */
        @JsonProperty("category")
        private String category;

        /**
         * 방문 시간
         */
        @JsonProperty("visitTime")
        private String visitTime;

        /**
         * 권장 체류 시간
         */
        @JsonProperty("duration")
        private String duration;

        /**
         * 위치 설명 또는 추가 정보
         */
        @JsonProperty("description")
        private String description;

        /**
         * 방문 순서
         */
        @JsonProperty("sequence")
        private Integer sequence;
    }
}

