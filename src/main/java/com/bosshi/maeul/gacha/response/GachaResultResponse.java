package com.bosshi.maeul.gacha.response;

import com.bosshi.maeul.gacha.GachaDraw;
import com.bosshi.maeul.gacha.GachaType;
import com.bosshi.maeul.gacha.RewardType;

import java.time.LocalDateTime;

/**
 * 가챠 결과. seenAt 이 null 이면 아직 확인하지 않은 결과다.
 *
 * @param amount    POINT: 포인트, FAT_EXP: 펫 경험치, 그 밖에는 null
 * @param voucherId PRIZE 일 때 발급한 교환권
 */
public record GachaResultResponse(
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
    public static GachaResultResponse from(GachaDraw draw) {
        return new GachaResultResponse(draw.resultId(), draw.gachaType(), draw.rewardType(), draw.label(), draw.amount(),
                draw.prizeId(), draw.itemId(), draw.voucherId(), draw.drawnAt(), draw.seenAt());
    }
}
