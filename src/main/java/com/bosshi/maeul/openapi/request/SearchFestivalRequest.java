package com.bosshi.maeul.openapi.request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

@Getter
@Setter
public class SearchFestivalRequest {
    private Integer numOfRows = 10;
    private Integer pageNo = 1;
    private String arrange = "C";
    private String eventStartDate;
    private String eventEndDate;
    private String areaCode;
    private String sigunguCode;
    private String cat1;
    private String cat2;
    private String cat3;
    private String lDongRegnCd;
    private String lDongSigunguCd;
    private String lclsSystm1;
    private String lclsSystm2;
    private String lclsSystm3;
    private String modifiedtime;

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

    private void addIfHasText(MultiValueMap<String, String> params, String key, String value) {
        if (StringUtils.hasText(value)) {
            params.add(key, value);
        }
    }
}
