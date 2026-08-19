package com.bosshi.maeul.plan.service;

import com.bosshi.maeul.openapi.request.AreaBasedSyncListRequest;
import com.bosshi.maeul.plan.request.PlacePreferenceRequest;
import com.bosshi.maeul.plan.response.RecommendedPlace;
import com.bosshi.maeul.openapi.service.OpenApiService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class PlaceRecommendationServiceTest {

    @Mock
    private OpenApiService openApiService;

    @Test
    void recommendPlacesReturnsRankedPlacesByPreference() {
        PlaceRecommendationService service = new PlaceRecommendationService(openApiService);
        AreaBasedSyncListRequest request = new AreaBasedSyncListRequest();

        PlacePreferenceRequest preference = new PlacePreferenceRequest();
        preference.setPreferredContentTypeId("12");
        preference.setPreferredKeywords(List.of("한옥", "공원"));
        preference.setMaxResults(2);

        given(openApiService.getAreaBasedSyncList(request)).willReturn(sampleResponse());

        List<RecommendedPlace> places = service.recommendPlaces(request, preference);

        assertThat(places).hasSize(2);
        assertThat(places.get(0).getTitle()).isEqualTo("북촌 한옥마을");
        assertThat(places.get(0).getScore()).isEqualTo(3);
        assertThat(places.get(1).getTitle()).isEqualTo("올림픽공원");
        assertThat(places.get(1).getScore()).isEqualTo(3);
    }

    @Test
    void recommendPlacesReturnsEmptyWhenItemsMissing() {
        PlaceRecommendationService service = new PlaceRecommendationService(openApiService);
        AreaBasedSyncListRequest request = new AreaBasedSyncListRequest();

        PlacePreferenceRequest preference = new PlacePreferenceRequest();
        preference.setPreferredKeywords(List.of("맛집"));

        given(openApiService.getAreaBasedSyncList(request))
                .willReturn("{\"response\":{\"body\":{\"items\":{}}}}\n");

        List<RecommendedPlace> places = service.recommendPlaces(request, preference);

        assertThat(places).isEmpty();
    }

    private String sampleResponse() {
        return """
                {
                  "response": {
                    "body": {
                      "items": {
                        "item": [
                          {
                            "title": "북촌 한옥마을",
                            "contentid": "1001",
                            "contenttypeid": "12",
                            "addr1": "서울 종로구",
                            "firstimage": "https://example.com/1.jpg",
                            "cat1": "A01"
                          },
                          {
                            "title": "올림픽공원",
                            "contentid": "1002",
                            "contenttypeid": "12",
                            "addr1": "서울 송파구",
                            "firstimage": "https://example.com/2.jpg",
                            "cat1": "A01"
                          },
                          {
                            "title": "테스트 숙소",
                            "contentid": "1003",
                            "contenttypeid": "32",
                            "addr1": "서울 강남구",
                            "firstimage": "https://example.com/3.jpg",
                            "cat1": "B02"
                          }
                        ]
                      }
                    }
                  }
                }
                """;
    }
}


