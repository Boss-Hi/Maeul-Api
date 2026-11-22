package com.bosshi.maeul.territory.service;

import com.bosshi.maeul.gacha.GachaDraw;
import com.bosshi.maeul.gacha.GachaDrawer;
import com.bosshi.maeul.gacha.GachaType;
import com.bosshi.maeul.gacha.RewardType;
import com.bosshi.maeul.itineraries.itinerary.event.MissionCompletedEvent;
import com.bosshi.maeul.itineraries.openapi.entity.RegionSubdivisions;
import com.bosshi.maeul.itineraries.openapi.repository.LDongSigunguRepository;
import com.bosshi.maeul.territory.entity.Fat;
import com.bosshi.maeul.territory.entity.FatMissionExp;
import com.bosshi.maeul.territory.repository.FatMissionExpRepository;
import com.bosshi.maeul.territory.repository.FatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("미션 가챠 경험치 테스트")
class FatMissionRewarderTest {

    private final FatRepository fatRepository = mock(FatRepository.class);
    private final FatMissionExpRepository missionExpRepository = mock(FatMissionExpRepository.class);
    private final LDongSigunguRepository sigunguRepository = mock(LDongSigunguRepository.class);
    private final GachaDrawer gachaDrawer = mock(GachaDrawer.class);
    private final FatMissionRewarder rewarder = new FatMissionRewarder(fatRepository, missionExpRepository, sigunguRepository, gachaDrawer);

    private final MissionCompletedEvent event = new MissionCompletedEvent(7L, 12L, "11", "560");

    @BeforeEach
    void setUp() {
        RegionSubdivisions subdivision = RegionSubdivisions.builder().code("560").name("영등포구").build();
        subdivision.setId(100L);
        when(sigunguRepository.findWithRegionByRegionCodeAndCode("11", "560")).thenReturn(Optional.of(subdivision));
    }

    @Test
    @DisplayName("그 시군구에 키우는 펫이 없으면 가챠를 뽑지 않는다")
    void noFatNoDraw() {
        when(fatRepository.findByUserIdAndRegionSubdivisionIdAndGraduatedAtIsNull(7L, 100L)).thenReturn(Optional.empty());

        rewarder.onMissionCompleted(event);

        verifyNoInteractions(gachaDrawer);
        verify(missionExpRepository, never()).save(any());
    }

    @Test
    @DisplayName("키우는 펫이 있으면 미션 가챠를 뽑아 나온 경험치를 그 펫에게 기록한다")
    void recordsDrawnExpOnFat() {
        when(fatRepository.findByUserIdAndRegionSubdivisionIdAndGraduatedAtIsNull(7L, 100L))
                .thenReturn(Optional.of(Fat.builder().id(3L).userId(7L).regionSubdivisionId(100L).build()));
        when(gachaDrawer.draw(7L, GachaType.MISSION, "mission:12")).thenReturn(
                new GachaDraw(55L, GachaType.MISSION, RewardType.FAT_EXP, "펫 경험치 6시간", 360L, null, null, null, null, null));

        rewarder.onMissionCompleted(event);

        ArgumentCaptor<FatMissionExp> saved = ArgumentCaptor.forClass(FatMissionExp.class);
        verify(missionExpRepository).save(saved.capture());
        assertThat(saved.getValue().getFatId()).isEqualTo(3L);
        assertThat(saved.getValue().getExp()).isEqualTo(360L);
        assertThat(saved.getValue().getGachaResultId()).isEqualTo(55L);
    }

    @Test
    @DisplayName("이미 경험치를 준 미션이면 다시 뽑지 않는다")
    void sameMissionOnce() {
        when(missionExpRepository.existsByUserIdAndItineraryMissionId(7L, 12L)).thenReturn(true);

        rewarder.onMissionCompleted(event);

        verifyNoInteractions(gachaDrawer);
    }
}
