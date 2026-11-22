package com.bosshi.maeul.gacha.dto;

import com.bosshi.maeul.gacha.type.VoucherStatus;

import java.time.LocalDateTime;

/** 교환권과 그 상품 */
public record Voucher(Long voucherId, Long prizeId, String prizeName, VoucherStatus status,
                      LocalDateTime issuedAt, LocalDateTime fulfilledAt) {
}
