package com.bosshi.maeul.openapi.request;

import org.junit.jupiter.api.Test;
import org.springframework.util.MultiValueMap;

import static org.assertj.core.api.Assertions.assertThat;

class AreaBasedSyncListRequestTest {

    @Test
    void toQueryParamsIncludesDefaultsAndOptionalValues() {
        AreaBasedSyncListRequest request = new AreaBasedSyncListRequest();
        request.setPageNo(2);
        request.setNumOfRows(20);
        request.setContentTypeId("12");
        request.setLDongRegnCd("26");
        request.setLDongSignguCd("380");
        request.setLclsSystm1("NA");
        request.setLclsSystm2("NA04");
        request.setLclsSystm3("NA040500");

        MultiValueMap<String, String> params = request.toQueryParams();

        assertThat(params.getFirst("numOfRows")).isEqualTo("20");
        assertThat(params.getFirst("pageNo")).isEqualTo("2");
        assertThat(params.getFirst("arrange")).isEqualTo("C");
        assertThat(params.getFirst("contentTypeId")).isEqualTo("12");
        assertThat(params.getFirst("lDongRegnCd")).isEqualTo("26");
        assertThat(params.getFirst("lDongSignguCd")).isEqualTo("380");
        assertThat(params.getFirst("lclsSystm1")).isEqualTo("NA");
        assertThat(params.getFirst("lclsSystm2")).isEqualTo("NA04");
        assertThat(params.getFirst("lclsSystm3")).isEqualTo("NA040500");
    }

    @Test
    void toQueryParamsOmitsNullOrEmptyValues() {
        AreaBasedSyncListRequest request = new AreaBasedSyncListRequest();
        request.setContentTypeId("12");

        MultiValueMap<String, String> params = request.toQueryParams();

        assertThat(params.getFirst("numOfRows")).isEqualTo("10");
        assertThat(params.getFirst("pageNo")).isEqualTo("1");
        assertThat(params.getFirst("arrange")).isEqualTo("C");
        assertThat(params.getFirst("contentTypeId")).isEqualTo("12");
        assertThat(params.containsKey("modifiedtime")).isFalse();
        assertThat(params.containsKey("lDongRegnCd")).isFalse();
    }
}

