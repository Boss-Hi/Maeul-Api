package com.bosshi.maeul.openapi.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OpenApiBaseResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void deserializesAreaTarSvcDemListResponseWithArrayItems() throws Exception {
        String json = """
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
                      "numOfRows": 1,
                      "pageNo": 1,
                      "totalCount": 1
                    }
                  }
                }
                """;

        AreaTarSvcDemListResponse response = objectMapper.readValue(json, AreaTarSvcDemListResponse.class);

        assertThat(response).isNotNull();
        assertThat(response.getResponse().getHeader().getResultCode()).isEqualTo("0000");
        assertThat(response.getResponse().getBody().getItems().getItem()).hasSize(1);
        assertThat(response.getResponse().getBody().getItems().getItem().get(0).getTarSvcDemIxVal()).isEqualTo("71.46");
    }

    @Test
    void deserializesAreaTarSvcDemListResponseWithEmptyStringItems() throws Exception {
        String json = """
                {
                  "response": {
                    "header": {
                      "resultCode": "0000",
                      "resultMsg": "OK"
                    },
                    "body": {
                      "items": "",
                      "numOfRows": 0,
                      "pageNo": 1,
                      "totalCount": 0
                    }
                  }
                }
                """;

        AreaTarSvcDemListResponse response = objectMapper.readValue(json, AreaTarSvcDemListResponse.class);

        assertThat(response).isNotNull();
        assertThat(response.getResponse().getHeader().getResultCode()).isEqualTo("0000");
        assertThat(response.getResponse().getBody().getItems().getItem()).isEmpty();
    }

    @Test
    void deserializesSearchFestivalResponseWithSingleObjectItem() throws Exception {
        String json = """
                {
                  "response": {
                    "header": {
                      "resultCode": "0000",
                      "resultMsg": "OK"
                    },
                    "body": {
                      "items": {
                        "item": {
                          "title": "단일 축제",
                          "contentid": "12345"
                        }
                      },
                      "numOfRows": 1,
                      "pageNo": 1,
                      "totalCount": 1
                    }
                  }
                }
                """;

        SearchFestivalResponse response = objectMapper.readValue(json, SearchFestivalResponse.class);

        assertThat(response).isNotNull();
        assertThat(response.getResponse().getBody().getItems().getItem()).hasSize(1);
        assertThat(response.getResponse().getBody().getItems().getItem().get(0).getTitle()).isEqualTo("단일 축제");
    }
}
