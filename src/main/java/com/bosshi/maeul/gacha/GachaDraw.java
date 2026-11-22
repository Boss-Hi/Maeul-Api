package com.bosshi.maeul.gacha;

import java.time.LocalDateTime;

/**
 * 뽑힌 가챠 결과. 뽑는 순간 정해진 값이다.
 *
 * @param amount    POINT: 포인트, FAT_EXP: 펫 경험치, 그 밖에는 null
 * @param voucherId PRIZE 일 때 발급한 교환권
 */
public record GachaDraw(
        Long resultId,
        GachaType gachaType,
        RewardType rewardType,
        String label,
        Long amount,
        Long prizeId,
        Long itemId,
        Long voucherId,
        LocalDateTime drawnAt,
        LocalDateTime seenAt
) {
}
