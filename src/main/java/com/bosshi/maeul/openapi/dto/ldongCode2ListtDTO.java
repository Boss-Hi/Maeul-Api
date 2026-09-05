package com.bosshi.maeul.openapi.dto;

import com.bosshi.maeul.openapi.request.OpenApiBaseRequest;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * 법정동 코드 조회(ldongCode2) API 요청 파라미터 DTO.
 */
@Getter
@Setter
@SuperBuilder
public class ldongCode2ListtDTO extends OpenApiBaseRequest {
    /**
     * 법정동 시도코드 (lDongRegnCd 해당되는 법정동 시군구코드 조회, 입력이 없을시 전체 시도목록 호출)
     *
     */
    private String lDongRegnCd;
    /**
     * 법정동 목록조회 여부(N:코드조회, Y:전체목록조회)
     *
     */
    @Builder.Default
    private String lDongListYn = "Y";
}
