package com.bosshi.maeul.kakao;

import com.bosshi.maeul.kakao.config.KakaoRestApiConfig;
import com.bosshi.maeul.kakao.response.KakaoRegionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.NamedInterface;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;

@NamedInterface
@Service
@RequiredArgsConstructor
public class KakaoClient {
    private final KakaoRestApiConfig kakaoRestApiConfig;

    public KakaoRegionResponse call(String endpoint, Map<String, Object> params) {
        RestClient restClient = RestClient.create();

        // Query Parameter 동적 빌드
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(kakaoRestApiConfig.getBaseUrl() + endpoint);

        if (params != null) {
            params.forEach(builder::queryParam);
        }

        URI uri = builder.build().encode().toUri();

        return restClient.get()
                .uri(uri)
                .header("Authorization", "KakaoAK " + kakaoRestApiConfig.getKey())
                .retrieve()
                .body(KakaoRegionResponse.class);
    }
}
