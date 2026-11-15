package com.bosshi.maeul.territory.response;

import com.bosshi.maeul.territory.dto.FatStatus;
import com.bosshi.maeul.territory.service.FatLevelTable;

import java.time.LocalDateTime;

/**
 * 키우는 중인 병아리
 *
 * @param exp           분양 이후 이 시군구의 점령 시간(분)
 * @param levelExp      지금 레벨에서 쌓은 경험치
 * @param nextLevelExp  지금 레벨에서 다음 레벨까지 필요한 경험치 (최대 레벨이면 null)
 * @param foundingLevel 건국 레벨 (레벨표에 없으면 null)
 * @param exchangeable  건국 레벨 이상이라 상품 교환(졸업)이 가능한지
 */
public record FatResponse(
        Long fatId,
        String regionId,
        String regionName,
        LocalDateTime adoptedAt,
        int level,
        String stageName,
        long exp,
        long levelExp,
        Long nextLevelExp,
        boolean maxLevel,
        Integer foundingLevel,
        boolean exchangeable
) {
    public static FatResponse from(FatStatus fat) {
        FatLevelTable.Progress progress = fat.progress();
        return new FatResponse(fat.fatId(), fat.regionId(), fat.regionName(), fat.adoptedAt(),
                progress.level(), progress.stageName(), progress.exp(), progress.levelExp(), progress.nextLevelExp(),
                progress.maxLevel(), progress.foundingLevel(), progress.founded());
    }
}
