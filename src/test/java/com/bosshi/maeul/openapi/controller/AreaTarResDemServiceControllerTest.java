package com.bosshi.maeul.openapi.controller;

import com.bosshi.maeul.openapi.request.AreaTarSvcDemListRequest;
import com.bosshi.maeul.openapi.response.AreaTarSvcDemListResponse;
import com.bosshi.maeul.openapi.service.OpenApiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AreaTarResDemServiceControllerTest {

    @Mock
    private OpenApiService openApiService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AreaTarResDemServiceController(openApiService)).build();
    }

    @Test
    void getAreaTarSvcDemListReturnsOpenApiResponse() throws Exception {
        given(openApiService.getAreaTarSvcDemList(argThat(matchesAreaTarSvcDemListRequest())))
                .willReturn(createAreaTarSvcDemListResponse());

        mockMvc.perform(get("/api/open/area-tar-svc-dem-list")
                        .queryParam("baseYm", "202409")
                        .queryParam("areaCd", "11")
                        .queryParam("signguCd", "11530")
                        .queryParam("tarSvcDemIxCd", "1101")
                        .queryParam("pageNo", "1")
                        .queryParam("numOfRows", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                          "response": {
                            "header": {
                              "resultCode": "0000",
                              "resultMsg": "OK"
                            },
                            "body": {
                              "items": {
                                "item": [
                                  {
                                    "baseYm": "202409",
                                    "areaCd": "11",
                                    "areaNm": "서울특별시",
                                    "signguCd": "11530",
                                    "signguNm": "구로구",
                                    "tarSvcDemIxCd": "1101",
                                    "tarSvcDemIxNm": "레포츠여행유형 SNS언급량",
                                    "tarSvcDemIxVal": "71.46"
                                  }
                                ]
                              },
                              "numOfRows": 10,
                              "pageNo": 1,
                              "totalCount": 1
                            }
                          }
                        }
                        """));

        then(openApiService).should().getAreaTarSvcDemList(argThat(matchesAreaTarSvcDemListRequest()));
    }

    @Test
    void getAreaTarSvcDemListReturnsBadRequestWhenRequiredFieldsMissing() throws Exception {
        mockMvc.perform(get("/api/open/area-tar-svc-dem-list")
                        .queryParam("baseYm", "202409"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("baseYm and areaCd are required."));

        then(openApiService).should(never()).getAreaTarSvcDemList(org.mockito.ArgumentMatchers.any());
    }

    private ArgumentMatcher<AreaTarSvcDemListRequest> matchesAreaTarSvcDemListRequest() {
        return request ->
                "202409".equals(request.getBaseYm()) &&
                "11".equals(request.getAreaCd()) &&
                "11530".equals(request.getSignguCd()) &&
                "1101".equals(request.getTarSvcDemIxCd()) &&
                Integer.valueOf(1).equals(request.getPageNo()) &&
                Integer.valueOf(10).equals(request.getNumOfRows());
    }

    private AreaTarSvcDemListResponse createAreaTarSvcDemListResponse() {
        AreaTarSvcDemListResponse response = new AreaTarSvcDemListResponse();
        AreaTarSvcDemListResponse.Response responseBody = new AreaTarSvcDemListResponse.Response();
        AreaTarSvcDemListResponse.Header header = new AreaTarSvcDemListResponse.Header();
        header.setResultCode("0000");
        header.setResultMsg("OK");

        AreaTarSvcDemListResponse.Item item = new AreaTarSvcDemListResponse.Item();
        item.setBaseYm("202409");
        item.setAreaCd("11");
        item.setAreaNm("서울특별시");
        item.setSignguCd("11530");
        item.setSignguNm("구로구");
        item.setTarSvcDemIxCd("1101");
        item.setTarSvcDemIxNm("레포츠여행유형 SNS언급량");
        item.setTarSvcDemIxVal("71.46");

        AreaTarSvcDemListResponse.Items items = new AreaTarSvcDemListResponse.Items();
        items.setItem(List.of(item));

        AreaTarSvcDemListResponse.Body body = new AreaTarSvcDemListResponse.Body();
        body.setItems(items);
        body.setNumOfRows(10);
        body.setPageNo(1);
        body.setTotalCount(1);

        responseBody.setHeader(header);
        responseBody.setBody(body);
        response.setResponse(responseBody);
        return response;
    }
}
