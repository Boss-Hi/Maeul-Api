package com.bosshi.maeul.territory.repository.dto;

import java.time.LocalDate;

/** 날짜별 점령 시간(초) */
public record DateStay(LocalDate date, Long seconds) {
}
