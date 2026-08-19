package com.bosshi.maeul.openapi.request;

import org.junit.jupiter.api.Test;
import org.springframework.util.MultiValueMap;

import static org.assertj.core.api.Assertions.assertThat;

class AreaTarSvcDemListRequestTest {

    @Test
    void toQueryParamsIncludesDefaultsAndOptionalValues() {
        AreaTarSvcDemListRequest request = new AreaTarSvcDemListRequest();
        request.setBaseYm("202509");
        request.setAreaCd("11");
        request.setSignguCd("11530");
        request.setTarSvcDemIxCd("1101");
        request.setPageNo(2);
        request.setNumOfRows(20);

        MultiValueMap<String, String> params = request.toQueryParams();

        assertThat(params.getFirst("numOfRows")).isEqualTo("20");
        assertThat(params.getFirst("pageNo")).isEqualTo("2");
        assertThat(params.getFirst("baseYm")).isEqualTo("202509");
        assertThat(params.getFirst("areaCd")).isEqualTo("11");
        assertThat(params.getFirst("signguCd")).isEqualTo("11530");
        assertThat(params.getFirst("tarSvcDemIxCd")).isEqualTo("1101");
    }

    @Test
    void toQueryParamsOmitsNullOrEmptyValues() {
        AreaTarSvcDemListRequest request = new AreaTarSvcDemListRequest();
        request.setBaseYm("202509");
        request.setAreaCd("11");

        MultiValueMap<String, String> params = request.toQueryParams();

        assertThat(params.getFirst("numOfRows")).isEqualTo("10");
        assertThat(params.getFirst("pageNo")).isEqualTo("1");
        assertThat(params.getFirst("baseYm")).isEqualTo("202509");
        assertThat(params.getFirst("areaCd")).isEqualTo("11");
        assertThat(params.containsKey("signguCd")).isFalse();
        assertThat(params.containsKey("tarSvcDemIxCd")).isFalse();
    }
}
