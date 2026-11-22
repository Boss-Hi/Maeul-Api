package com.bosshi.maeul.territory.response;

import com.bosshi.maeul.gacha.GachaDraw;
import com.bosshi.maeul.gacha.RewardType;
import com.bosshi.maeul.territory.dto.Graduation;

/**
 * 졸업 결과: 졸업한 펫과 그때 뽑은 졸업 가챠 결과
 */
public record GraduationResponse(GraduatedFatResponse fat, Gacha gacha) {

    public static GraduationResponse from(Graduation graduation) {
        return new GraduationResponse(GraduatedFatResponse.from(graduation.fat()), Gacha.from(graduation.gacha()));
    }

    /**
     * @param amount    POINT: 포인트, 그 밖에는 null
     * @param voucherId PRIZE 일 때 발급한 교환권
     */
    public record Gacha(Long resultId, RewardType rewardType, String label, Long amount, Long prizeId, Long itemId, Long voucherId) {
        static Gacha from(GachaDraw draw) {
            return new Gacha(draw.resultId(), draw.rewardType(), draw.label(), draw.amount(), draw.prizeId(), draw.itemId(), draw.voucherId());
        }
    }
}
