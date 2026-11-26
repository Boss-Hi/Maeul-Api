package com.bosshi.maeul.gacha.dto;

import com.bosshi.maeul.gacha.entity.OfflinePrizeKind;

/**
 * 오프라인 상품 종류와 상품 QR 개수. 재고는 발급한 상품 QR 개수다.
 *
 * @param issued    발급한 상품 QR 개수
 * @param delivered 그중 지급 완료된 개수
 */
public record PrizeKindStock(OfflinePrizeKind kind, long issued, long delivered) {
}
