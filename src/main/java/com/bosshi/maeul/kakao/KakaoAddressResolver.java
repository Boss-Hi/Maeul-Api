package com.bosshi.maeul.kakao;

import com.bosshi.maeul.kakao.response.KakaoRegionResponse;
import com.bosshi.maeul.location.dto.LocationDTO;
import com.bosshi.maeul.location.impl.AddressResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("kakaoAddressResolver")
@RequiredArgsConstructor
public class KakaoAddressResolver implements AddressResolver {

    private static final String REGION_CODE_ENDPOINT = "/v2/local/geo/coord2regioncode.json";
    private final KakaoClient kakaoClient;

    @Override
    public LocationDTO getRegionAddress(Double latitude, Double longitude) {
        // 1. Kakao API 파라미터 구성 (x: 경도, y: 위도)
        Map<String, Object> params = Map.of(
                "x", longitude,
                "y", latitude
        );

        // 2. KakaoClient call 호출
        KakaoRegionResponse response = kakaoClient.call(REGION_CODE_ENDPOINT, params);

        // 3. 디테일한 응답 데이터 가공 및 예외 처리
        if (response == null || response.getDocuments() == null || response.getDocuments().isEmpty()) {
            return LocationDTO.builder()
                    .region1depthName("알 수 없는 지역")
                    .region2depthName("")
                    .build();
        }

        // 법정동("B") 우선 선택, 없을 경우 첫 번째 요소 사용
        KakaoRegionResponse.Document document = response.getDocuments().stream()
                .filter(doc -> "B".equals(doc.getRegionType()))
                .findFirst()
                .orElse(response.getDocuments().get(0));

        return LocationDTO.builder()
                .region1depthName(document.getRegion1depthName())
                .region2depthName(document.getRegion2depthName())
                .build();
    }
}
