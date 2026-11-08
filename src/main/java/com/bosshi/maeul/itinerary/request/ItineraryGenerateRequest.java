package com.bosshi.maeul.itinerary.request;

import com.bosshi.maeul.itinerary.dto.ItineraryGenerateDTO;
import com.bosshi.maeul.itinerary.type.Pace;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.AssertTrue;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
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
public class ItineraryGenerateRequest {

    @JsonProperty("tour_id")
    private String tourId;

    /**
     * 여행 시작일
     * 예: "2026-09-01"
     */
    @JsonFormat(pattern = "yyyyMMdd")
    private LocalDate startDate;

    /**
     * 여행 종료일
     * 예: "2026-09-10"
     */
    @JsonFormat(pattern = "yyyyMMdd")
    private LocalDate endDate;

    /**
     * 선택한 카테고리 목록
     * 예: ["축제/공연", "체험관광/공예", "숙박/호텔"]
     */
    @JsonProperty("selectedCategories")
    private List<String> selectedCategories;

    /**
     * 하루 일정 페이스. 없으면 설문의 일정 템포, 그것도 없으면 MODERATE
     */
    private Pace pace;

    /**
     * 하루 시작 시간. 없으면 09:30
     * 예: "09:30"
     */
    @JsonFormat(pattern = "HH:mm")
    private LocalTime dayStartTime;

    /**
     * 하루 마감 시간. 없으면 22:30
     * 예: "22:30"
     */
    @JsonFormat(pattern = "HH:mm")
    private LocalTime dayEndTime;

    @JsonIgnore
    @AssertTrue(message = "dayEndTime 은 dayStartTime 보다 늦어야 합니다.")
    public boolean isDayWindowValid() {
        LocalTime start = dayStartTime != null ? dayStartTime : ItineraryGenerateDTO.DEFAULT_DAY_START;
        LocalTime end = dayEndTime != null ? dayEndTime : ItineraryGenerateDTO.DEFAULT_DAY_END;
        return start.isBefore(end);
    }

}

