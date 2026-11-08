package com.bosshi.maeul.itineraries.itinerary.response;

import com.bosshi.maeul.itineraries.itinerary.dto.ItinerarySettings;
import com.bosshi.maeul.itineraries.itinerary.type.Pace;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

/**
 * 일정 설정 화면의 "하루 일정 페이스 & 시간대 선호".
 * 생성 요청에는 pace(value), dayStartTime, dayEndTime(HH:mm)을 그대로 보낸다. 보내지 않으면 기본값이 쓰인다.
 *
 * @param paces         선택할 수 있는 페이스
 * @param defaultPace   미리 선택해 둘 페이스 (설문의 일정 템포, 없으면 MODERATE)
 * @param dayStartTime  하루 시작 시간
 * @param dayEndTime    하루 마감 시간
 */
public record ItinerarySettingsResponse(
        List<PaceOption> paces,
        Pace defaultPace,
        TimeOption dayStartTime,
        TimeOption dayEndTime
) {
    private static final String DAY_START_LABEL = "일반적인 시작";
    private static final String DAY_END_LABEL = "축제/야경 감상 후";

    /**
     * @param value       생성 요청의 pace 에 보내는 값
     * @param title       화면에 보여줄 이름
     * @param description 화면에 보여줄 설명
     */
    public record PaceOption(Pace value, String title, String description) {
    }

    /**
     * @param label        기본값을 설명하는 문구
     * @param defaultValue 기본 시간 (HH:mm)
     */
    public record TimeOption(String label, @JsonFormat(pattern = "HH:mm") LocalTime defaultValue) {
    }

    public static ItinerarySettingsResponse from(ItinerarySettings settings) {
        return new ItinerarySettingsResponse(
                Arrays.stream(Pace.values()).map(pace -> new PaceOption(pace, pace.title(), pace.description())).toList(),
                settings.defaultPace(),
                new TimeOption(DAY_START_LABEL, settings.defaultDayStartTime()),
                new TimeOption(DAY_END_LABEL, settings.defaultDayEndTime()));
    }
}
