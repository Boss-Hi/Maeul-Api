package com.bosshi.maeul.gacha.response;

import com.bosshi.maeul.gacha.entity.PrizeQr;

import java.time.LocalDateTime;

public record PrizeDeliveryResponse(Long prizeQrId, Long kindId, Long ticketId, LocalDateTime deliveredAt) {

    public static PrizeDeliveryResponse from(PrizeQr qr) {
        return new PrizeDeliveryResponse(qr.getId(), qr.getKindId(), qr.getTicketId(), qr.getDeliveredAt());
    }
}
