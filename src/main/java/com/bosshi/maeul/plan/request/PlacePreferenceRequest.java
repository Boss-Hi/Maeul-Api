package com.bosshi.maeul.plan.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 사용자 취향 기반 장소 추출 기준.
 */
@Getter
@Setter
public class PlacePreferenceRequest {
    /** 선호 관광타입 ID (예: 12 관광지) */
    private String preferredContentTypeId;
    /** 제목/카테고리에서 매칭할 키워드 목록 */
    private List<String> preferredKeywords;
    /** 최대 추출 개수 (기본: 10) */
    private Integer maxResults = 10;
}

