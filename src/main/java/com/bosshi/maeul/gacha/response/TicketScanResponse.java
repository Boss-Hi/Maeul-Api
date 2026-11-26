package com.bosshi.maeul.gacha.response;

import com.bosshi.maeul.gacha.entity.GachaTicket;

import java.time.LocalDateTime;

public record TicketScanResponse(Long ticketId, LocalDateTime usedAt) {

    public static TicketScanResponse from(GachaTicket ticket) {
        return new TicketScanResponse(ticket.getId(), ticket.getUsedAt());
    }
}
