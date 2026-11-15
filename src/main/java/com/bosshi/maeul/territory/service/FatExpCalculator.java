package com.bosshi.maeul.territory.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 펫 경험치 = 머문 시간(분) + 방문일 수 x 방문일 경험치. 둘 다 분양 이후이면서 일정 기간 안인 날만 센다 (ADR 0013).
 * 일정 기간은 시작일 0시부터 종료일 24시까지이므로 날짜 단위로 판단하고, 겹치는 기간은 한 번만 센다.
 */
public final class FatExpCalculator {

    private FatExpCalculator() {
    }

    public record Period(LocalDate startDate, LocalDate endDate) {
        boolean covers(LocalDate date) {
            return !date.isBefore(startDate) && !date.isAfter(endDate);
        }
    }

    /**
     * @param adoptionDaySeconds 분양 당일, 분양 시각 이후 그 시군구의 점령 시간(초)
     * @param dailySeconds       분양 다음 날부터 날짜별 그 시군구의 점령 시간(초)
     */
    public static long exp(LocalDate adoptionDate, long adoptionDaySeconds, Map<LocalDate, Long> dailySeconds,
                           List<Period> periods, long visitDayExp) {
        long staySeconds = 0;
        long visitDays = 0;
        if (covered(adoptionDate, periods) && adoptionDaySeconds > 0) {
            staySeconds += adoptionDaySeconds;
            visitDays++;
        }
        for (Map.Entry<LocalDate, Long> day : dailySeconds.entrySet()) {
            if (day.getKey().isAfter(adoptionDate) && day.getValue() > 0 && covered(day.getKey(), periods)) {
                staySeconds += day.getValue();
                visitDays++;
            }
        }
        return staySeconds / 60 + visitDays * visitDayExp;
    }

    private static boolean covered(LocalDate date, List<Period> periods) {
        return periods.stream().anyMatch(period -> period.covers(date));
    }
}
