package com.bosshi.maeul.openapi.request;

import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import org.springframework.modulith.NamedInterface;

/**
 * 공공데이터포털 행사정보조회 API 요청 파라미터를 담는 DTO.
 * REST API 요청 시 Spring이 바인딩하고, 최종적으로 MultiValueMap으로 변환해 호출한다.
 */
@NamedInterface
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Accessors(chain = true)
public class SearchFestivalRequest{
    /**
     * 행사 시작일 (YYYYMMDD)
     */
    private String eventStartDate;
    /**
     * 행사 종료일 (YYYYMMDD)
     */
    private String eventEndDate;
    /**
     * 법정동 시군구 코드
     */
    private String lDongRegnCd;
    /**
     * 법정동 지역 코드
     */
    private String lDongSigunguCd;
    /**
     * 카테고리 Code
     */
    private String tourCategoryCode;

    /** 한 페이지 결과 수 (기본: 10) */
    @Builder.Default
    private Integer numOfRows = 10;
    /** 페이지 번호 (기본: 1) */
    @Builder.Default
    private Integer pageNo = 1;
}
