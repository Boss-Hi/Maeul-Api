package com.bosshi.maeul.gacha.request;

import jakarta.validation.constraints.NotBlank;

public record ScanTicketRequest(@NotBlank String code) {
}
