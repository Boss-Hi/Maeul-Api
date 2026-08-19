package com.bosshi.maeul.openapi.request;

import lombok.Getter;
import lombok.Setter;

/**
 * 지역기반 관광정보조회(areaBasedSyncList2) API 요청 파라미터 DTO.
 */
@Getter
@Setter
public class AreaBasedSyncListRequest extends OpenApiBaseRequest {
    /** 정렬구분 (A=제목순, C=수정일순, D=생성일순, O/Q/R=대표이미지 필수) */
    private String arrange = "C";
    /** 관광타입 ID */
    private String contentTypeId;
    /** 미사용 예정: 지역 코드 */
    private String areaCode;
    /** 미사용 예정: 시군구 코드 */
    private String sigunguCode;
    /** 미사용 예정: 대분류 코드 */
    private String cat1;
    /** 미사용 예정: 중분류 코드 */
    private String cat2;
    /** 미사용 예정: 소분류 코드 */
    private String cat3;
    /** 수정일 (YYYYMMDD) */
    private String modifiedtime;
    /** 법정동 시도 코드 */
    private String lDongRegnCd;
    /** 법정동 시군구 코드 (lDongRegnCd와 함께 사용) */
    private String lDongSignguCd;
    /** 분류체계 1Depth */
    private String lclsSystm1;
    /** 분류체계 2Depth (lclsSystm1 필수) */
    private String lclsSystm2;
    /** 분류체계 3Depth (lclsSystm1, lclsSystm2 필수) */
    private String lclsSystm3;
}

