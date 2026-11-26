package com.bosshi.maeul.gacha.controller;

import com.bosshi.maeul.auth.CustomUserDetails;
import com.bosshi.maeul.common.response.ApiResponse;
import com.bosshi.maeul.gacha.TicketEventType;
import com.bosshi.maeul.gacha.dto.PrizeKindStock;
import com.bosshi.maeul.gacha.request.AddStoreOwnerRequest;
import com.bosshi.maeul.gacha.request.CreatePrizeKindRequest;
import com.bosshi.maeul.gacha.request.CreateStoreRequest;
import com.bosshi.maeul.gacha.request.IssuePrizeQrsRequest;
import com.bosshi.maeul.gacha.request.ReasonRequest;
import com.bosshi.maeul.gacha.request.UpdateTicketRuleRequest;
import com.bosshi.maeul.gacha.response.PrizeKindResponse;
import com.bosshi.maeul.gacha.response.PrizeQrResponse;
import com.bosshi.maeul.gacha.response.StoreOwnerResponse;
import com.bosshi.maeul.gacha.response.StoreResponse;
import com.bosshi.maeul.gacha.response.TicketRuleResponse;
import com.bosshi.maeul.gacha.service.GachaTicketService;
import com.bosshi.maeul.gacha.service.PrizeQrService;
import com.bosshi.maeul.gacha.service.QrImageService;
import com.bosshi.maeul.gacha.service.StoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 운영자: 오프라인 가챠 관리 (ADR 0016). 매장과 사장님, 상품 QR 발급, 사용과 지급 취소, 가챠권 발급 설정
 */
@RestController
@RequestMapping("/api/admin/gacha")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class OfflineGachaAdminController {
    private final StoreService storeService;
    private final PrizeQrService prizeQrService;
    private final GachaTicketService ticketService;
    private final QrImageService qrImageService;

    /** 매장 등록. regionCode 와 code 는 region_subdivisions 의 시도 코드와 시군구 코드를 각각 보낸다 */
    @PostMapping("/stores")
    public ResponseEntity<ApiResponse<StoreResponse>> createStore(@Valid @RequestBody CreateStoreRequest request) {
        return ApiResponse.success(StoreResponse.from(storeService.register(request.name(), request.regionCode(), request.code())));
    }

    @GetMapping("/stores")
    public ResponseEntity<ApiResponse<List<StoreResponse>>> stores() {
        return ApiResponse.success(storeService.stores().stream().map(StoreResponse::from).toList());
    }

    /** 사용자를 매장의 사장님으로 연결한다. 그 사용자의 역할(STORE_OWNER)은 따로 지정해야 스캔할 수 있다 */
    @PostMapping("/stores/{storeId}/owners")
    public ResponseEntity<ApiResponse<StoreOwnerResponse>> addOwner(@PathVariable Long storeId, @Valid @RequestBody AddStoreOwnerRequest request) {
        return ApiResponse.success(StoreOwnerResponse.from(storeService.addOwner(storeId, request.userId())));
    }

    @GetMapping("/stores/{storeId}/owners")
    public ResponseEntity<ApiResponse<List<StoreOwnerResponse>>> owners(@PathVariable Long storeId) {
        return ApiResponse.success(storeService.owners(storeId).stream().map(StoreOwnerResponse::from).toList());
    }

    @PostMapping("/prize-kinds")
    public ResponseEntity<ApiResponse<PrizeKindResponse>> createKind(@Valid @RequestBody CreatePrizeKindRequest request) {
        return ApiResponse.success(PrizeKindResponse.from(new PrizeKindStock(prizeQrService.createKind(request.name()), 0, 0)));
    }

    /** 상품 종류와 발급한 상품 QR 개수(재고), 지급 완료 개수 */
    @GetMapping("/prize-kinds")
    public ResponseEntity<ApiResponse<List<PrizeKindResponse>>> kinds() {
        return ApiResponse.success(prizeQrService.kinds().stream().map(PrizeKindResponse::from).toList());
    }

    /** 상품 QR 대량 발급 (한 번에 최대 1000개). 응답의 code 를 QR 로 인쇄한다 */
    @PostMapping("/prize-kinds/{kindId}/qrs")
    public ResponseEntity<ApiResponse<List<PrizeQrResponse>>> issueQrs(@PathVariable Long kindId, @Valid @RequestBody IssuePrizeQrsRequest request) {
        return ApiResponse.success(prizeQrService.issue(kindId, request.count()).stream().map(PrizeQrResponse::from).toList());
    }

    @GetMapping("/prize-kinds/{kindId}/qrs")
    public ResponseEntity<ApiResponse<Page<PrizeQrResponse>>> qrs(@PathVariable Long kindId, @PageableDefault(size = 50) Pageable pageable) {
        return ApiResponse.success(prizeQrService.qrs(kindId, pageable).map(PrizeQrResponse::from));
    }

    /** 상품 QR 인쇄용 이미지 (PNG) */
    @GetMapping(value = "/prize-qrs/{prizeQrId}/qr.png", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> qrImage(@PathVariable Long prizeQrId) {
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(qrImageService.png(prizeQrService.qr(prizeQrId).getCode()));
    }

    /** 가챠권 사용 취소. 취소된 가챠권은 다시 쓸 수 있다. 상품이 연결돼 있으면 상품 지급을 먼저 취소해야 한다 */
    @PostMapping("/tickets/{ticketId}/cancel-use")
    public ResponseEntity<ApiResponse<Void>> cancelUse(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long ticketId,
            @Valid @RequestBody ReasonRequest request
    ) {
        ticketService.cancelUse(ticketId, userDetails.getId(), request.reason());
        return ApiResponse.success(null);
    }

    /** 상품 QR 지급 취소. 그 상품 QR 은 다시 쓸 수 있고 가챠권과의 연결이 끊어진다 */
    @PostMapping("/prize-qrs/{prizeQrId}/cancel-delivery")
    public ResponseEntity<ApiResponse<Void>> cancelDelivery(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long prizeQrId,
            @Valid @RequestBody ReasonRequest request
    ) {
        prizeQrService.cancelDelivery(prizeQrId, userDetails.getId(), request.reason());
        return ApiResponse.success(null);
    }

    @GetMapping("/ticket-rules/{eventType}")
    public ResponseEntity<ApiResponse<TicketRuleResponse>> rule(@PathVariable TicketEventType eventType) {
        return ApiResponse.success(TicketRuleResponse.from(ticketService.rule(eventType)));
    }

    /** 가챠권 발급 설정. 바꾼 값은 그 뒤 발급분부터 적용된다 */
    @PutMapping("/ticket-rules/{eventType}")
    public ResponseEntity<ApiResponse<TicketRuleResponse>> updateRule(@PathVariable TicketEventType eventType, @Valid @RequestBody UpdateTicketRuleRequest request) {
        return ApiResponse.success(TicketRuleResponse.from(ticketService.updateRule(eventType, request.enabled(), request.validityDays())));
    }
}
