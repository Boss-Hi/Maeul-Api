package com.bosshi.maeul.gacha.response;

import com.bosshi.maeul.gacha.dto.PrizeKindStock;

public record PrizeKindResponse(Long id, String name, long issued, long delivered) {

    public static PrizeKindResponse from(PrizeKindStock stock) {
        return new PrizeKindResponse(stock.kind().getId(), stock.kind().getName(), stock.issued(), stock.delivered());
    }
}
