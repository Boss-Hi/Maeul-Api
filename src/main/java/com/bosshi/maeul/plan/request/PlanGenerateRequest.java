package com.bosshi.maeul.plan.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

/**
 * 여행 일정 추천 요청 DTO (Step 1: 기본 정보 입력)
 * <p>
 * 사용자가 기본 정보, 지역, 카테고리를 선택할 때 사용됩니다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanGenerateRequest {

    @JsonProperty("festival_id")
    private String festivalId;

    /**
     * 여행 시작일
     * 예: "2026-09-01"
     */
    @JsonProperty("startDate")
    private String startDate;

    /**
     * 여행 종료일
     * 예: "2026-09-10"
     */
    @JsonProperty("endDate")
    private String endDate;

    /**
     * 선택한 카테고리 목록
     * 예: ["축제/공연", "체험관광/공예", "숙박/호텔"]
     */
    @JsonProperty("selectedCategories")
    private List<String> selectedCategories;

}

