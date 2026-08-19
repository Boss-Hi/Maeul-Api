package com.bosshi.maeul.openapi.controller;

import com.bosshi.maeul.openapi.request.SearchFestivalRequest;
import com.bosshi.maeul.openapi.response.SearchFestivalResponse;
import com.bosshi.maeul.openapi.service.OpenApiService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@ExtendWith(MockitoExtension.class)
class OpenApiControllerTest {

    @Mock
    private OpenApiService openApiService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new KorService2Controller(openApiService)).build();
    }

    @Test
    void searchFestivalReturnsOpenApiResponse() throws Exception {
        given(openApiService.searchFestival(argThat(matchesFestivalRequest())))
                .willReturn(createFestivalResponse());

        mockMvc.perform(get("/api/open/search-festival")
                        .queryParam("eventStartDate", "20260101")
                        .queryParam("eventEndDate", "20261201")
                        .queryParam("pageNo", "2")
                        .queryParam("numOfRows", "20")
                        .queryParam("arrange", "A")
                        .queryParam("lDongRegnCd", "11")
                        .queryParam("lDongSigunguCd", "470"))
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
                                    "title": "시흥갯골축제",
                                    "contentid": "142197"
                                  }
                                ]
                              },
                              "numOfRows": 20,
                              "pageNo": 2,
                              "totalCount": 1
                            }
                          }
                        }
                        """));

        then(openApiService).should().searchFestival(argThat(matchesFestivalRequest()));
    }

    @Test
    void searchFestivalReturnsBadRequestWhenRequiredDatesMissing() throws Exception {
        mockMvc.perform(get("/api/open/search-festival")
                        .queryParam("eventStartDate", "20260101"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("eventStartDate and eventEndDate are required."));

        then(openApiService).should(never()).searchFestival(org.mockito.ArgumentMatchers.any());
    }

    private ArgumentMatcher<SearchFestivalRequest> matchesFestivalRequest() {
        return request ->
                "20260101".equals(request.getEventStartDate()) &&
                "20261201".equals(request.getEventEndDate()) &&
                Integer.valueOf(2).equals(request.getPageNo()) &&
                Integer.valueOf(20).equals(request.getNumOfRows()) &&
                "A".equals(request.getArrange()) &&
                "11".equals(request.getLDongRegnCd()) &&
                "470".equals(request.getLDongSigunguCd());
    }

    private SearchFestivalResponse createFestivalResponse() {
        SearchFestivalResponse response = new SearchFestivalResponse();
        SearchFestivalResponse.Response responseBody = new SearchFestivalResponse.Response();
        SearchFestivalResponse.Header header = new SearchFestivalResponse.Header();
        header.setResultCode("0000");
        header.setResultMsg("OK");

        SearchFestivalResponse.Item item = new SearchFestivalResponse.Item();
        item.setTitle("시흥갯골축제");
        item.setContentid("142197");

        SearchFestivalResponse.Items items = new SearchFestivalResponse.Items();
        items.setItem(java.util.List.of(item));

        SearchFestivalResponse.Body body = new SearchFestivalResponse.Body();
        body.setItems(items);
        body.setNumOfRows(20);
        body.setPageNo(2);
        body.setTotalCount(1);

        responseBody.setHeader(header);
        responseBody.setBody(body);
        response.setResponse(responseBody);
        return response;
    }
}
