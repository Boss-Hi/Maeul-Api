package com.bosshi.maeul.itineraries.itinerary.photomission;

/**
 * AI 가 기준 사진들과 올린 사진을 비교한 결과.
 *
 * @param same         기준 사진 중 하나라도 같은 장소(지점)를 찍은 사진인가
 * @param confidence   같은 장소라는 확신 (0~1)
 * @param matchedIndex 같다고 본 기준 사진의 번호(1부터). 없으면 0
 * @param reason       판단 사유. 서버 기록용이며 사용자에게 보이지 않는다
 */
public record PhotoJudgement(boolean same, double confidence, int matchedIndex, String reason) {
}
