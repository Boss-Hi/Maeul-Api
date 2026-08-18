package com.bosshi.maeul.openapi.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 공공데이터포털 행사정보조회 API 응답 DTO.
 * JSON 구조는 response.header / response.body / response.body.items.item 형태로 내려온다.
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchFestivalResponse {
    /** API 최상위 응답 래퍼 */
    private Response response;

    /** 응답의 헤더와 바디를 담는 내부 응답 객체 */
    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Response {
        /** 요청 결과 코드와 메시지 */
        private Header header;
        /** 실제 행사 결과 목록 */
        private Body body;
    }

    /** 결과 코드와 결과 메시지 */
    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Header {
        private String resultCode;
        private String resultMsg;
    }

    /** 검색 결과 본문 */
    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Body {
        /** 행사 목록 */
        private Items items;
        /** 요청한 페이지당 결과 수 */
        private Integer numOfRows;
        /** 요청 페이지 번호 */
        private Integer pageNo;
        /** 전체 결과 수 */
        private Integer totalCount;
    }

    /** 행사 목록 배열 */
    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Items {
        private List<Item> item;
    }

    /** 단일 행사 정보 */
    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Item {
        private String addr1;
        private String addr2;
        private String zipcode;
        private String cat1;
        private String cat2;
        private String cat3;
        private String contentid;
        private String contenttypeid;
        private String createdtime;
        private String eventstartdate;
        private String eventenddate;
        private String firstimage;
        private String firstimage2;
        private String cpyrhtDivCd;
        private String mapx;
        private String mapy;
        private String mlevel;
        private String modifiedtime;
        private String areacode;
        private String sigungucode;
        private String tel;
        private String title;
        private String lDongRegnCd;
        private String lDongSignguCd;
        private String lclsSystm1;
        private String lclsSystm2;
        private String lclsSystm3;
        private String progresstype;
        private String festivaltype;
    }
}
