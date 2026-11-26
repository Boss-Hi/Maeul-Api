package com.bosshi.maeul.gacha.response;

import com.bosshi.maeul.gacha.entity.PrizeQr;

import java.time.LocalDateTime;

public record PrizeQrResponse(Long id, Long kindId, String code, boolean delivered, LocalDateTime deliveredAt, Long ticketId) {

    public static PrizeQrResponse from(PrizeQr qr) {
        return new PrizeQrResponse(qr.getId(), qr.getKindId(), qr.getCode(), qr.getDeliveredAt() != null, qr.getDeliveredAt(), qr.getTicketId());
    }
}
