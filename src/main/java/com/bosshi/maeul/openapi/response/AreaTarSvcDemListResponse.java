package com.bosshi.maeul.openapi.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

/**
 * 지역별 관광 서비스 수요 정보 목록 조회(areaTarSvcDemList) API 응답 DTO.
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class AreaTarSvcDemListResponse extends OpenApiBaseResponse<AreaTarSvcDemListResponse.Item> {

    /** 단일 지역별 관광 서비스 수요 정보 */
    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Item {
        /** 조회 기준 연월 (YYYYMM) */
        private String baseYm;
        /** 지역 코드 */
        private String areaCd;
        /** 지역명 */
        private String areaNm;
        /** 시군구 코드 */
        private String signguCd;
        /** 시군구명 */
        private String signguNm;
        /** 관광 서비스 수요 지표 코드 */
        private String tarSvcDemIxCd;
        /** 관광 서비스 수요 지표명 */
        private String tarSvcDemIxNm;
        /** 관광 서비스 수요 지표값 */
        private String tarSvcDemIxVal;
    }
}
