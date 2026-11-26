package com.bosshi.maeul.gacha.request;

import jakarta.validation.constraints.NotBlank;

public record CreatePrizeKindRequest(@NotBlank String name) {
}
