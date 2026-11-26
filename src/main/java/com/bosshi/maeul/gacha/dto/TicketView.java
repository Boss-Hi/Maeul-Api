package com.bosshi.maeul.gacha.dto;

import com.bosshi.maeul.gacha.entity.GachaTicket;
import com.bosshi.maeul.gacha.type.TicketStatus;

/**
 * 내 가챠권과 지금 상태
 *
 * @param prizeName 상품 QR 이 연결된 가챠권의 상품 이름, 아니면 null
 */
public record TicketView(GachaTicket ticket, TicketStatus status, String prizeName) {
}
