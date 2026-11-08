package com.bosshi.maeul.itineraries.itinerary.type;

import java.util.Optional;

/**
 * 하루 일정의 밀도
 */
public enum Pace {
    /** 느긋하게 */
    RELAXED("슬로우/힐링", "하루 2~3개 장소", "하루 2-3곳", 3),
    /** 적당히 알차게 */
    MODERATE("적당히 알차게", "하루 4~5개 장소", "하루 4-5곳", 5),
    /** 촘촘하게 */
    PACKED("빡빡한 정복형", "알뜰한 풀코스", "하루 6곳 이상", null);

    private final String title;
    private final String description;
    private final String venueRange;
    private final Integer maxVenuesPerDay;

    Pace(String title, String description, String venueRange, Integer maxVenuesPerDay) {
        this.title = title;
        this.description = description;
        this.venueRange = venueRange;
        this.maxVenuesPerDay = maxVenuesPerDay;
    }

    /** 화면에 보여줄 이름 */
    public String title() {
        return title;
    }

    /** 화면에 보여줄 설명 */
    public String description() {
        return description;
    }

    /** 프롬프트에 쓰는 하루 장소 수 표현 */
    public String venueRange() {
        return venueRange;
    }

    /** 하루 최대 장소 수. 제한이 없으면 null */
    public Integer maxVenuesPerDay() {
        return maxVenuesPerDay;
    }

    /** 설문 옵션 값(relaxed / moderate / packed) 에 대응하는 페이스 */
    public static Optional<Pace> fromSurveyValue(String value) {
        for (Pace pace : values()) {
            if (pace.name().equalsIgnoreCase(value)) {
                return Optional.of(pace);
            }
        }
        return Optional.empty();
    }
}
