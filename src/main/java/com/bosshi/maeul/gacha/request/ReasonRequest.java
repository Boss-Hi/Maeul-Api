package com.bosshi.maeul.gacha.request;

import jakarta.validation.constraints.NotBlank;

public record ReasonRequest(@NotBlank String reason) {
}
