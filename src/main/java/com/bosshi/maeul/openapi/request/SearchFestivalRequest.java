package com.bosshi.maeul.openapi.request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

/**
 * 공공데이터포털 행사정보조회 API 요청 파라미터를 담는 DTO.
 * REST API 요청 시 Spring이 바인딩하고, 최종적으로 MultiValueMap으로 변환해 호출한다.
 */
@Getter
@Setter
public class SearchFestivalRequest {
    /** 기본 페이지 크기: 10건 */
    private Integer numOfRows = 10;
    /** 기본 페이지 번호: 1페이지 */
    private Integer pageNo = 1;
    /** 정렬 기준: C = 수정일순 */
    private String arrange = "C";
    /** 행사 시작일 (YYYYMMDD) */
    private String eventStartDate;
    /** 행사 종료일 (YYYYMMDD) */
    private String eventEndDate;
    /** 지역코드 */
    private String areaCode;
    /** 시군구코드 */
    private String sigunguCode;
    /** 대분류 코드 */
    private String cat1;
    /** 중분류 코드 */
    private String cat2;
    /** 소분류 코드 */
    private String cat3;
    /** 법정동 시군구 코드 */
    private String lDongRegnCd;
    /** 법정동 지역 코드 */
    private String lDongSigunguCd;
    /** 1단계 분류 체계 */
    private String lclsSystm1;
    /** 2단계 분류 체계 */
    private String lclsSystm2;
    /** 3단계 분류 체계 */
    private String lclsSystm3;
    /** 수정일 기준 필터 */
    private String modifiedtime;

    /**
     * API 호출용 query parameter로 변환한다.
     * null 또는 빈 값은 제외해 불필요한 파라미터 전송을 방지한다.
     */
    public MultiValueMap<String, String> toQueryParams() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("numOfRows", String.valueOf(numOfRows));
        params.add("pageNo", String.valueOf(pageNo));
        params.add("arrange", arrange);
        addIfHasText(params, "eventStartDate", eventStartDate);
        addIfHasText(params, "eventEndDate", eventEndDate);
        addIfHasText(params, "areaCode", areaCode);
        addIfHasText(params, "sigunguCode", sigunguCode);
        addIfHasText(params, "cat1", cat1);
        addIfHasText(params, "cat2", cat2);
        addIfHasText(params, "cat3", cat3);
        addIfHasText(params, "lDongRegnCd", lDongRegnCd);
        addIfHasText(params, "lDongSigunguCd", lDongSigunguCd);
        addIfHasText(params, "lclsSystm1", lclsSystm1);
        addIfHasText(params, "lclsSystm2", lclsSystm2);
        addIfHasText(params, "lclsSystm3", lclsSystm3);
        addIfHasText(params, "modifiedtime", modifiedtime);
        return params;
    }

    /** 값이 존재할 때만 query parameter에 추가한다. */
    private void addIfHasText(MultiValueMap<String, String> params, String key, String value) {
        if (StringUtils.hasText(value)) {
            params.add(key, value);
        }
    }
}
