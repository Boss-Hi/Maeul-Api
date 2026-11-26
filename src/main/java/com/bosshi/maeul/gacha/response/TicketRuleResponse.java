package com.bosshi.maeul.gacha.response;

import com.bosshi.maeul.gacha.TicketEventType;
import com.bosshi.maeul.gacha.entity.GachaTicketRule;

public record TicketRuleResponse(TicketEventType eventType, boolean enabled, int validityDays) {

    public static TicketRuleResponse from(GachaTicketRule rule) {
        return new TicketRuleResponse(rule.getEventType(), rule.isEnabled(), rule.getValidityDays());
    }
}
