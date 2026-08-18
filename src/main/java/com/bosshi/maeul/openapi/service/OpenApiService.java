package com.bosshi.maeul.openapi.service;

import com.bosshi.maeul.openapi.config.OpenApiProperties;
import com.bosshi.maeul.openapi.request.SearchFestivalRequest;
import com.bosshi.maeul.openapi.response.SearchFestivalResponse;
import com.bosshi.maeul.openapi.type.TourApiEndpoint;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OpenApiService {
    private final RestClient.Builder restClientBuilder;
    private final OpenApiProperties openApiProperties;

    public SearchFestivalResponse searchFestival(SearchFestivalRequest request) {
        return call(TourApiEndpoint.SEARCH_FESTIVAL, request.toQueryParams(), SearchFestivalResponse.class);
    }

    public String call(TourApiEndpoint endpoint, MultiValueMap<String, String> requestParams) {
        return call(endpoint, requestParams, String.class);
    }

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

        URI uri = UriComponentsBuilder
                .fromUriString(openApiProperties.getBaseUrl())
                .pathSegment(endpoint.getPath())
                .queryParams(queryParams)
                .build(true)
                .toUri();

        return restClientBuilder
                .build()
                .get()
                .uri(uri)
                .retrieve()
                .body(responseType);
    }

    private void putIfMissing(MultiValueMap<String, String> params, String key, String value) {
        List<String> values = params.get(key);
        if ((values == null || values.isEmpty()) && StringUtils.hasText(value)) {
            params.add(key, value);
        }
    }

    private void validateServiceKey() {
        if (!StringUtils.hasText(openApiProperties.getServiceKey())) {
            throw new IllegalStateException("OPEN_API_SECRET_KEY environment variable is required.");
        }
    }
}
