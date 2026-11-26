package com.bosshi.maeul.gacha.request;

import jakarta.validation.constraints.NotNull;

public record UpdateTicketRuleRequest(@NotNull Boolean enabled, @NotNull Integer validityDays) {
}
