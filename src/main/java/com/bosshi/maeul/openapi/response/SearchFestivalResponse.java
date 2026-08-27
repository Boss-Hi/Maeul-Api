package com.bosshi.maeul.openapi.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import org.springframework.modulith.NamedInterface;

/**
 * 공공데이터포털 행사정보조회 API 응답 DTO.
 * JSON 구조는 response.header / response.body / response.body.items.item 형태로 내려온다.
 */
@NamedInterface
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchFestivalResponse extends OpenApiBaseResponse<SearchFestivalResponse.Item> {

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
