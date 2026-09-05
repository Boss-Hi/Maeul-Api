package com.bosshi.maeul.openapi.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

/**
 * 법정동 코드 조회(ldongCode2) API 응답 DTO.
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ldongCode2ListtResponse extends OpenApiBaseResponse<ldongCode2ListtResponse.Item> {

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Item {
        private String lDongRegnCd;
        private String lDongRegnNm;
        private String lDongSignguCd;
        private String lDongSignguNm;
        private int rnum;

    }
}
