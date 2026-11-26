package com.bosshi.maeul.territory.dto;

import com.bosshi.maeul.gacha.GachaDraw;

/** 졸업한 펫과 그때 뽑은 졸업 가챠 결과 */
public record Graduation(GraduatedFat fat, GachaDraw gacha) {
}
