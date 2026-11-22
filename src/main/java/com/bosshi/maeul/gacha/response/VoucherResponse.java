package com.bosshi.maeul.gacha.response;

import com.bosshi.maeul.gacha.dto.Voucher;
import com.bosshi.maeul.gacha.type.VoucherStatus;

import java.time.LocalDateTime;

/** 교환권. status 는 ISSUED(발급됨) 또는 FULFILLED(지급 완료) */
public record VoucherResponse(Long voucherId, Long prizeId, String prizeName, VoucherStatus status,
                              LocalDateTime issuedAt, LocalDateTime fulfilledAt) {

    public static VoucherResponse from(Voucher voucher) {
        return new VoucherResponse(voucher.voucherId(), voucher.prizeId(), voucher.prizeName(), voucher.status(),
                voucher.issuedAt(), voucher.fulfilledAt());
    }
}
