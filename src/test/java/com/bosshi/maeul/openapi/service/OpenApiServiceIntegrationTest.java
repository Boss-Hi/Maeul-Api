package com.bosshi.maeul.openapi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.bosshi.maeul.openapi.config.OpenApiProperties;
import com.bosshi.maeul.openapi.request.AreaTarSvcDemListRequest;
import com.bosshi.maeul.openapi.request.SearchFestivalRequest;
import com.bosshi.maeul.openapi.response.AreaTarSvcDemListResponse;
import com.bosshi.maeul.openapi.response.SearchFestivalResponse;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StringUtils;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class OpenApiServiceIntegrationTest {

    @Autowired
    private OpenApiService openApiService;

    @Autowired
    private OpenApiProperties openApiProperties;

    @Test
    void searchFestivalCanPreviewRealOpenApiResponse() throws Exception {
        Assumptions.assumeTrue(
                StringUtils.hasText(openApiProperties.getServiceKey()),
                "OPEN_API_SECRET_KEY is required to run the real OpenAPI preview test."
        );

        SearchFestivalRequest request = new SearchFestivalRequest();
        request.setEventStartDate("20260101");
        request.setEventEndDate("20261231");
        request.setNumOfRows(3);
        request.setPageNo(1);

        SearchFestivalResponse response = openApiService.searchFestival(request);
        System.out.println("OpenAPI festival preview response:");
        System.out.println(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(response));

        assertThat(response).isNotNull();
        assertThat(response.getResponse()).isNotNull();
        assertThat(response.getResponse().getHeader().getResultCode()).isEqualTo("0000");
        assertThat(response.getResponse().getBody().getItems().getItem()).isNotEmpty();
    }

    @Test
    void getAreaTarSvcDemListCanPreviewRealOpenApiResponse() throws Exception {
        Assumptions.assumeTrue(
                StringUtils.hasText(openApiProperties.getServiceKey()),
                "OPEN_API_SECRET_KEY is required to run the real OpenAPI preview test."
        );

        AreaTarSvcDemListRequest request = new AreaTarSvcDemListRequest();
        request.setBaseYm("202409");
        request.setAreaCd("11");
        request.setSignguCd("11530");
        request.setTarSvcDemIxCd("1101");
        request.setNumOfRows(3);
        request.setPageNo(1);

        AreaTarSvcDemListResponse response = openApiService.getAreaTarSvcDemList(request);
        System.out.println("OpenAPI AreaTarSvcDemList preview response:");
        System.out.println(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(response));

        assertThat(response).isNotNull();
        assertThat(response.getResponse()).isNotNull();
        assertThat(response.getResponse().getHeader().getResultCode()).isEqualTo("0000");
        assertThat(response.getResponse().getBody().getItems().getItem()).isNotEmpty();
    }
}
