package com.bosshi.maeul.gacha.response;

import com.bosshi.maeul.gacha.entity.OfflineGachaLog;
import com.bosshi.maeul.gacha.type.OfflineGachaAction;

import java.time.LocalDateTime;

public record OfflineGachaLogResponse(Long id, OfflineGachaAction action, Long ticketId, Long prizeQrId, Long storeId, Long actorUserId, String reason, LocalDateTime createdAt) {

    public static OfflineGachaLogResponse from(OfflineGachaLog log) {
        return new OfflineGachaLogResponse(log.getId(), log.getAction(), log.getTicketId(), log.getPrizeQrId(), log.getStoreId(), log.getActorUserId(), log.getReason(), log.getCreatedAt());
    }
}
