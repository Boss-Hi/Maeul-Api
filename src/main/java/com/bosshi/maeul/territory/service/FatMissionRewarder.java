package com.bosshi.maeul.territory.service;

import com.bosshi.maeul.gacha.GachaDraw;
import com.bosshi.maeul.gacha.GachaDrawer;
import com.bosshi.maeul.gacha.GachaType;
import com.bosshi.maeul.gacha.RewardType;
import com.bosshi.maeul.itineraries.itinerary.event.MissionCompletedEvent;
import com.bosshi.maeul.itineraries.openapi.repository.LDongSigunguRepository;
import com.bosshi.maeul.territory.entity.Fat;
import com.bosshi.maeul.territory.entity.FatMissionExp;
import com.bosshi.maeul.territory.repository.FatMissionExpRepository;
import com.bosshi.maeul.territory.repository.FatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 미션을 완료하면 그 시군구에서 키우는 펫이 있을 때만 미션 가챠를 뽑고, 나온 경험치를 그 펫에게 기록한다 (ADR 0014).
 * 펫이 없으면 뽑지 않는다. 같은 미션은 한 번만 준다.
 */
@Component
@RequiredArgsConstructor
public class FatMissionRewarder {

    private final FatRepository fatRepository;
    private final FatMissionExpRepository missionExpRepository;
    private final LDongSigunguRepository sigunguRepository;
    private final GachaDrawer gachaDrawer;

    @EventListener
    @Transactional
    public void onMissionCompleted(MissionCompletedEvent event) {
        if (event.regionCode() == null || event.subdivisionCode() == null
                || missionExpRepository.existsByUserIdAndItineraryMissionId(event.userId(), event.itineraryMissionId())) {
            return;
        }
        Optional<Fat> fat = sigunguRepository.findWithRegionByRegionCodeAndCode(event.regionCode(), event.subdivisionCode())
                .flatMap(subdivision -> fatRepository.findByUserIdAndRegionSubdivisionIdAndGraduatedAtIsNull(event.userId(), subdivision.getId()));
        if (fat.isEmpty()) {
            return;
        }
        GachaDraw draw = gachaDrawer.draw(event.userId(), GachaType.MISSION, "mission:" + event.itineraryMissionId());
        if (draw.rewardType() != RewardType.FAT_EXP || draw.amount() == null) {
            return;
        }
        missionExpRepository.save(FatMissionExp.builder()
                .userId(event.userId())
                .fatId(fat.get().getId())
                .itineraryMissionId(event.itineraryMissionId())
                .gachaResultId(draw.resultId())
                .exp(draw.amount())
                .build());
    }
}
