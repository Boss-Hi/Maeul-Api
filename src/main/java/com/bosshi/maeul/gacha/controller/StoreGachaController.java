package com.bosshi.maeul.gacha.controller;

import com.bosshi.maeul.auth.CustomUserDetails;
import com.bosshi.maeul.common.response.ApiResponse;
import com.bosshi.maeul.gacha.request.ScanPrizeRequest;
import com.bosshi.maeul.gacha.request.ScanTicketRequest;
import com.bosshi.maeul.gacha.response.OfflineGachaLogResponse;
import com.bosshi.maeul.gacha.response.PrizeDeliveryResponse;
import com.bosshi.maeul.gacha.response.TicketScanResponse;
import com.bosshi.maeul.gacha.service.GachaTicketService;
import com.bosshi.maeul.gacha.service.PrizeQrService;
import com.bosshi.maeul.gacha.service.StoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 사장님: 가챠권 QR 과 상품 QR 스캔 (ADR 0016). 사장님 역할(STORE_OWNER)이고 매장에 연결된 계정만 쓸 수 있다
 */
@RestController
@RequestMapping("/api/store/gacha")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STORE_OWNER')")
public class StoreGachaController {
    private final GachaTicketService ticketService;
    private final PrizeQrService prizeQrService;
    private final StoreService storeService;

    /**
     * 가챠권 QR 스캔: 사용됨으로 바꾸고 ticketId 를 돌려준다. 이어서 나온 캡슐의 상품 QR 을 스캔할 때 이 값을 함께 보낸다
     */
    @PostMapping("/tickets/scan")
    public ResponseEntity<ApiResponse<TicketScanResponse>> scanTicket(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ScanTicketRequest request
    ) {
        return ApiResponse.success(TicketScanResponse.from(ticketService.use(userDetails.getId(), request.code())));
    }

    /**
     * 상품 QR 스캔: 사용된 가챠권(ticketId)과 연결하고 지급 완료로 바꾼다
     */
    @PostMapping("/prizes/scan")
    public ResponseEntity<ApiResponse<PrizeDeliveryResponse>> scanPrize(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ScanPrizeRequest request
    ) {
        return ApiResponse.success(PrizeDeliveryResponse.from(prizeQrService.deliver(userDetails.getId(), request.ticketId(), request.code())));
    }

    /**
     * 내 매장의 사용 이력 (최근 순). page(0부터), size(기본 20)
     */
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<Page<OfflineGachaLogResponse>>> history(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ApiResponse.success(storeService.history(userDetails.getId(), pageable).map(OfflineGachaLogResponse::from));
    }
}
