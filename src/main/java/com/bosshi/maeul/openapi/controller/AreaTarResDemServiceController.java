package com.bosshi.maeul.openapi.controller;

import com.bosshi.maeul.openapi.request.AreaTarSvcDemListRequest;
import com.bosshi.maeul.openapi.service.OpenApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/open")
@RequiredArgsConstructor
public class AreaTarResDemServiceController {
    private final OpenApiService openApiService;

    /**
     * 지역별 관광 서비스 수요 정보 목록 조회
     * 요청 파라미터:
     * - baseYm: 조회 기준 연월 (YYYYMM, 필수)
     * - areaCd: 지역 코드 (필수)
     * - signguCd: 시군구 코드 (선택)
     * - tarSvcDemIxCd: 관광 서비스 수요 지표 코드 (선택)
     * - numOfRows: 페이지당 결과 수 (기본: 10)
     * - pageNo: 페이지 번호 (기본: 1)
     */
    @GetMapping({"/area-tar-svc-dem-list", "/areaTarSvcDemList"})
    public ResponseEntity<?> getAreaTarSvcDemList(@ModelAttribute AreaTarSvcDemListRequest request) {
        if (!StringUtils.hasText(request.getBaseYm()) || !StringUtils.hasText(request.getAreaCd())) {
            return ResponseEntity.badRequest().body("baseYm and areaCd are required.");
        }

        return ResponseEntity.ok(openApiService.getAreaTarSvcDemList(request));
    }
}
