package com.bosshi.maeul.gacha.request;

import jakarta.validation.constraints.NotNull;

public record AddStoreOwnerRequest(@NotNull Long userId) {
}
