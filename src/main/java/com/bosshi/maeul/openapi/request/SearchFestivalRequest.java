package com.bosshi.maeul.openapi.request;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.modulith.NamedInterface;

/**
 * 공공데이터포털 행사정보조회 API 요청 파라미터를 담는 DTO.
 * REST API 요청 시 Spring이 바인딩하고, 최종적으로 MultiValueMap으로 변환해 호출한다.
 */
@NamedInterface
@Getter
@Setter
@Accessors(chain = true)
public class SearchFestivalRequest extends OpenApiBaseRequest {
    /**
     * 정렬 기준: C = 수정일순
     */
    private String arrange = "C";
    /**
     * 행사 시작일 (YYYYMMDD)
     */
    private String eventStartDate;
    /**
     * 행사 종료일 (YYYYMMDD)
     */
    private String eventEndDate;
    /**
     * 지역코드
     */
    private String areaCode;
    /**
     * 시군구코드
     */
    private String sigunguCode;
    /**
     * 대분류 코드
     */
    private String cat1;
    /**
     * 중분류 코드
     */
    private String cat2;
    /**
     * 소분류 코드
     */
    private String cat3;
    /**
     * 법정동 시군구 코드
     */
    private String lDongRegnCd;
    /**
     * 법정동 지역 코드
     */
    private String lDongSigunguCd;
    /**
     * 1단계 분류 체계
     */
    private String lclsSystm1;
    /**
     * 2단계 분류 체계
     */
    private String lclsSystm2;
    /**
     * 3단계 분류 체계
     */
    private String lclsSystm3;
}
