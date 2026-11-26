package com.bosshi.maeul.gacha.response;

import com.bosshi.maeul.gacha.dto.TicketView;
import com.bosshi.maeul.gacha.type.TicketStatus;

import java.time.LocalDateTime;

public record TicketResponse(Long id, String code, TicketStatus status, LocalDateTime expiresAt, LocalDateTime usedAt, String prizeName, LocalDateTime createdAt) {

    public static TicketResponse from(TicketView view) {
        return new TicketResponse(view.ticket().getId(), view.ticket().getCode(), view.status(), view.ticket().getExpiresAt(), view.ticket().getUsedAt(), view.prizeName(), view.ticket().getCreatedAt());
    }
}
