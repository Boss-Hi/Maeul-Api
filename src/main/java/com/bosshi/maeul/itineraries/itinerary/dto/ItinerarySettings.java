package com.bosshi.maeul.itineraries.itinerary.dto;

import com.bosshi.maeul.itineraries.itinerary.type.Pace;

import java.time.LocalTime;

/**
 * 일정 생성 설정 화면에서 사용자에게 미리 선택해 둘 값
 *
 * @param defaultPace         요청하지 않았을 때 쓰는 페이스 (설문의 일정 템포, 없으면 MODERATE)
 * @param defaultDayStartTime 요청하지 않았을 때의 하루 시작 시간
 * @param defaultDayEndTime   요청하지 않았을 때의 하루 마감 시간
 */
public record ItinerarySettings(Pace defaultPace, LocalTime defaultDayStartTime, LocalTime defaultDayEndTime) {
}
