package com.bosshi.maeul.openapi.service;

import com.bosshi.maeul.openapi.config.OpenApiProperties;
import com.bosshi.maeul.openapi.request.AreaBasedSyncListRequest;
import com.bosshi.maeul.openapi.request.AreaTarSvcDemListRequest;
import com.bosshi.maeul.openapi.request.SearchFestivalRequest;
import com.bosshi.maeul.openapi.response.AreaTarSvcDemListResponse;
import com.bosshi.maeul.openapi.response.SearchFestivalResponse;
import com.bosshi.maeul.openapi.type.TourApiEndpoint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.NamedInterface;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@NamedInterface
@Slf4j
@Service
@RequiredArgsConstructor
public class OpenApiService {
    private final RestClient.Builder restClientBuilder;
    private final OpenApiProperties openApiProperties;

    /**
     * 행사정보조회 API를 호출하고 DTO로 변환해 반환한다.
     * 요청 객체에 들어 있는 필드들을 query param으로 바꿔 외부 API에 전달한다.
     */
    public SearchFestivalResponse searchFestival(SearchFestivalRequest request) {
        return call(TourApiEndpoint.SEARCH_FESTIVAL, request.toQueryParams(), SearchFestivalResponse.class);
    }

    /**
     * 지역기반 관광정보조회(areaBasedSyncList2) API를 호출한다.
     */
    public String getAreaBasedSyncList(AreaBasedSyncListRequest request) {
        return call(TourApiEndpoint.AREA_BASED_SYNC_LIST, request.toQueryParams(), String.class);
    }

    /**
     * 지역별 관광 서비스 수요 정보 목록 조회 API를 호출하고 DTO로 변환해 반환한다.
     */
    public AreaTarSvcDemListResponse getAreaTarSvcDemList(AreaTarSvcDemListRequest request) {
        return call(TourApiEndpoint.AREA_TAR_SVC_DEM_LIST, request.toQueryParams(), AreaTarSvcDemListResponse.class);
    }

    /**
     * 문자열 응답이 필요한 단순 조회용 호출 메서드.
     * 보통 디버깅 또는 테스트 용도로 사용한다.
     */
    public String call(TourApiEndpoint endpoint, MultiValueMap<String, String> requestParams) {
        return call(endpoint, requestParams, String.class);
    }

    /**
     * 지정된 관광 API 엔드포인트를 호출해 원하는 응답 타입으로 매핑한다.
     * serviceKey, MobileOS, MobileApp, _type은 필수 파라미터이므로 누락 시 자동 보완한다.
     */
    public <T> T call(TourApiEndpoint endpoint, MultiValueMap<String, String> requestParams, Class<T> responseType) {
        validateServiceKey();

        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        if (requestParams != null) {
            queryParams.addAll(requestParams);
        }

        putIfMissing(queryParams, "serviceKey", openApiProperties.getServiceKey());
        putIfMissing(queryParams, "MobileOS", openApiProperties.getMobileOs());
        putIfMissing(queryParams, "MobileApp", openApiProperties.getMobileApp());
        putIfMissing(queryParams, "_type", openApiProperties.getResponseType());

        String baseUrl = openApiProperties.getBaseUrl();
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }

        URI uri = UriComponentsBuilder
                .fromUriString(baseUrl)
                .path(endpoint.getPath())
                .queryParams(queryParams)
                .build(true)
                .toUri();

        try {
            return restClientBuilder
                    .build()
                    .get()
                    .uri(uri)
                    .retrieve()
                    .body(responseType);
        } catch (RestClientResponseException e) {
            log.error("OpenAPI 호출 실패 - HTTP Status: {}, Request URI: {}, Response Body: {}",
                    e.getStatusCode(), uri, e.getResponseBodyAsString());
            throw e;
        }
    }

    /** 파라미터가 비어 있지 않을 때만 기본값을 추가한다. */
    private void putIfMissing(MultiValueMap<String, String> params, String key, String value) {
        List<String> values = params.get(key);
        if ((values == null || values.isEmpty()) && StringUtils.hasText(value)) {
            params.add(key, value);
        }
    }

    /** 서비스 키가 비어 있으면 로컬 개발/테스트 환경에서 명확한 오류를 남긴다. */
    private void validateServiceKey() {
        if (!StringUtils.hasText(openApiProperties.getServiceKey())) {
            throw new IllegalStateException("OPEN_API_SECRET_KEY environment variable is required.");
        }
    }
}
