package com.bosshi.maeul.gacha.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ScanPrizeRequest(@NotNull Long ticketId, @NotBlank String code) {
}
