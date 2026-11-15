package com.bosshi.maeul.territory.response;

import com.bosshi.maeul.territory.dto.MasterStatus;
import com.bosshi.maeul.territory.service.FatLevelTable;

/**
 * 내 주인
 *
 * @param exp          주인 경험치 (펫 경험치 합 x 주인 비율)
 * @param fatExpSum    바탕이 된 펫 경험치 합 (키우는 중은 지금 값, 졸업은 졸업 때 값)
 * @param levelExp     지금 레벨에서 쌓은 경험치
 * @param nextLevelExp 지금 레벨에서 다음 레벨까지 필요한 경험치 (최대 레벨이면 null)
 */
public record MasterResponse(int level, String stageName, long exp, long fatExpSum, long levelExp, Long nextLevelExp, boolean maxLevel) {

    public static MasterResponse from(MasterStatus master) {
        FatLevelTable.Progress p = master.progress();
        return new MasterResponse(p.level(), p.stageName(), p.exp(), master.fatExpSum(), p.levelExp(), p.nextLevelExp(), p.maxLevel());
    }
}
