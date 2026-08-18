package com.bosshi.maeul.openapi.request;

import org.junit.jupiter.api.Test;
import org.springframework.util.MultiValueMap;

import static org.assertj.core.api.Assertions.assertThat;

class SearchFestivalRequestTest {

    @Test
    void toQueryParamsIncludesDefaultsAndOptionalValues() {
        SearchFestivalRequest request = new SearchFestivalRequest();
        request.setEventStartDate("20260101");
        request.setEventEndDate("20261201");
        request.setLDongRegnCd("11");
        request.setLDongSigunguCd("470");
        request.setLclsSystm1("EV");

        MultiValueMap<String, String> params = request.toQueryParams();

        assertThat(params.getFirst("numOfRows")).isEqualTo("10");
        assertThat(params.getFirst("pageNo")).isEqualTo("1");
        assertThat(params.getFirst("arrange")).isEqualTo("C");
        assertThat(params.getFirst("eventStartDate")).isEqualTo("20260101");
        assertThat(params.getFirst("eventEndDate")).isEqualTo("20261201");
        assertThat(params.getFirst("lDongRegnCd")).isEqualTo("11");
        assertThat(params.getFirst("lDongSigunguCd")).isEqualTo("470");
        assertThat(params.getFirst("lclsSystm1")).isEqualTo("EV");
        assertThat(params.containsKey("sigunguCode")).isFalse();
    }
}
