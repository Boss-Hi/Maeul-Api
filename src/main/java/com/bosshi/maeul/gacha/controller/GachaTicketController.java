package com.bosshi.maeul.gacha.controller;

import com.bosshi.maeul.auth.CustomUserDetails;
import com.bosshi.maeul.common.response.ApiResponse;
import com.bosshi.maeul.qr.QrImageService;
import com.bosshi.maeul.gacha.entity.GachaTicket;
import com.bosshi.maeul.gacha.response.TicketResponse;
import com.bosshi.maeul.gacha.service.GachaTicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 오프라인 가챠권: 내 가챠권과 QR (ADR 0016). 사장님이 이 QR 을 스캔하면 사용됨이 된다
 */
@RestController
@RequestMapping("/api/gacha/tickets")
@RequiredArgsConstructor
public class GachaTicketController {
    private final GachaTicketService ticketService;
    private final QrImageService qrImageService;

    /**
     * 내 가챠권 (최근 순). code 가 QR 에 담길 값이다
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<TicketResponse>>> tickets(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.success(ticketService.myTickets(userDetails.getId()).stream().map(TicketResponse::from).toList());
    }

    /**
     * 가챠권 QR 이미지 (PNG)
     */
    @GetMapping(value = "/{ticketId}/qr.png", produces = MediaType.IMAGE_PNG_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> qr(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long ticketId) {
        GachaTicket ticket = ticketService.ownTicket(userDetails.getId(), ticketId);
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(qrImageService.png(ticket.getCode(), QrImageService.DEFAULT_SIZE));
    }
}
