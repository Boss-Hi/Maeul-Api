package com.bosshi.maeul.gacha.request;

import jakarta.validation.constraints.NotBlank;

public record CreateStoreRequest(@NotBlank String name, @NotBlank String regionCode, @NotBlank String code) {
}
