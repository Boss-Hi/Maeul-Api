package com.bosshi.maeul.openapi.dto;

import com.bosshi.maeul.openapi.request.OpenApiBaseRequest;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * 공통정보조회 조회(detailCommon2) API 요청 파라미터 DTO.
 */
@Getter
@Setter
@SuperBuilder
public class DetailCommon2DTO extends OpenApiBaseRequest {
    private String contentId;
}
