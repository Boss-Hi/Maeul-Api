package com.bosshi.maeul.plan.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 사용자 취향 매칭 결과로 반환할 최소 장소 정보.
 */
@Getter
@AllArgsConstructor
public class RecommendedPlace {
    private final String contentId;
    private final String title;
    private final String contentTypeId;
    private final String address;
    private final String firstImage;
    private final int score;
}

