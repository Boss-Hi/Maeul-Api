package com.bosshi.maeul.kakao.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class KakaoRegionResponse {

    private List<Document> documents;

    @Getter
    public static class Document {
        @JsonProperty("region_type")
        private String regionType;

        @JsonProperty("address_name")
        private String addressName;

        @JsonProperty("region_1depth_name")
        private String region1depthName; // 서울특별시

        @JsonProperty("region_2depth_name")
        private String region2depthName; // 중랑구

        @JsonProperty("region_3depth_name")
        private String region3depthName; // 면목동
    }
}