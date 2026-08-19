package com.bosshi.maeul.openapi.request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

/**
 * 지역별 관광 서비스 수요 정보 목록 조회(areaTarSvcDemList) API 요청 파라미터 DTO.
 */
@Getter
@Setter
public class AreaTarSvcDemListRequest extends OpenApiBaseRequest {
    /** 조회 기준 연월 (형식 : YYYYMM) */
    private String baseYm;
    /** 지역 코드 */
    private String areaCd;
    /** 시군구 코드 */
    private String signguCd;
    /**
     * 관광 서비스 수요 지표 코드
     * 11 : 전체,
     * 1101 : 레포츠 SNS 언급량, 1102 : 휴식 힐링 SNS 언급량, 1103 : 미식 SNS 언급량, 1104 : 체험 SNS 언급량,
     * 1105 : 쇼핑업 소비액, 1106 : 식음료 소비액, 1107 : 숙박업 소비액, 1108 : 여가 서비스업 소비액, 1109 : 운송업 소비액,
     * 1110 : 내비게이션 숙박 검색량, 1111 : 내비게이션 음식 검색량, 1112 : 내비게이션 쇼핑 검색량
     */
    private String tarSvcDemIxCd;
}
