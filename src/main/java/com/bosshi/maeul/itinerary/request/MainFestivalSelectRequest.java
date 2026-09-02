package com.bosshi.maeul.itinerary.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import java.time.LocalDate;

/**
 * 여행 일정 추천 요청 DTO (Step 2: MainFestival 선택)
 *
 * 사용자가 축제를 선택하고 일정 생성을 요청할 때 사용됩니다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MainFestivalSelectRequest {

    /**
     * Trip의 ID
     */
    @JsonProperty("tripId")
    private String tripId;

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
     * 전화번호
     */
    @JsonProperty("tel")
    private String tel;

    /**
     * 이미지 URL
     */
    @JsonProperty("imageUrl")
    private String imageUrl;

    /**
     * 입력 값 검증
     */
    public boolean isValid() {
        return tripId != null &&
                contentId != null &&
                title != null &&
                latitude != null &&
                longitude != null;
    }
}

