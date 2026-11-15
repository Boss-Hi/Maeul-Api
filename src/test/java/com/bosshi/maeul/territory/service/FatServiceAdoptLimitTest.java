package com.bosshi.maeul.territory.service;

import com.bosshi.maeul.itineraries.openapi.entity.Region;
import com.bosshi.maeul.itineraries.openapi.entity.RegionSubdivisions;
import com.bosshi.maeul.itineraries.openapi.repository.LDongSigunguRepository;
import com.bosshi.maeul.territory.config.TerritoryProperties;
import com.bosshi.maeul.territory.entity.FatSetting;
import com.bosshi.maeul.territory.repository.*;
import com.bosshi.maeul.timeline.config.TimelineProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("FatService 분양 조건 테스트")
class FatServiceAdoptLimitTest {

    private final FatRepository fatRepository = mock(FatRepository.class);
    private final FatSettingRepository settingRepository = mock(FatSettingRepository.class);
    private final LDongSigunguRepository sigunguRepository = mock(LDongSigunguRepository.class);
    private final FatService service = new FatService(fatRepository, mock(FatLevelRepository.class), settingRepository,
            mock(TerritoryDailyStayRepository.class), mock(TerritoryRegionCellRepository.class), mock(FatItineraryPeriodRepository.class), sigunguRepository,
            new TerritoryProperties(), new TimelineProperties());

    @BeforeEach
    void setUp() {
        when(sigunguRepository.findWithRegionByRegionCodeAndCode("11", "560")).thenReturn(Optional.of(subdivision("11", "560", "영등포구")));
    }

    @Test
    @DisplayName("키우는 병아리가 DB 설정의 최대 마리 수에 이르면 분양을 거부하고 저장하지 않는다")
    void rejectsAtLimit() {
        when(settingRepository.findFirstByDeletedAtIsNullOrderByIdAsc())
                .thenReturn(Optional.of(FatSetting.builder().maxActiveFats(2).build()));
        when(fatRepository.countByUserIdAndGraduatedAtIsNull(7L)).thenReturn(2L);

        assertThatThrownBy(() -> service.adopt(7L, "11", "560"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("2마리");
        verify(fatRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("region_code 와 code 로 찾을 수 없는 시군구는 분양을 거부한다")
    void rejectsUnknownRegion() {
        when(sigunguRepository.findWithRegionByRegionCodeAndCode("11", "999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.adopt(7L, "11", "999"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("존재하지 않는 시군구");
        verify(fatRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("아래에 구가 있는 시는 분양을 거부한다")
    void rejectsRegionWithoutOccupationCode() {
        when(sigunguRepository.findWithRegionByRegionCodeAndCode("41", "110")).thenReturn(Optional.of(subdivision("41", "110", "수원시")));
        when(sigunguRepository.hasDistrictsUnder("41", "수원시")).thenReturn(true);

        assertThatThrownBy(() -> service.adopt(7L, "41", "110"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("수원시에서는 병아리를 키울 수 없습니다");
        verify(fatRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("설정 행이 없으면 분양을 막는다")
    void failsWithoutSetting() {
        when(settingRepository.findFirstByDeletedAtIsNullOrderByIdAsc()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.adopt(7L, "11", "560")).isInstanceOf(IllegalStateException.class);
        verify(fatRepository, never()).saveAndFlush(any());
    }

    private static RegionSubdivisions subdivision(String regionCode, String code, String name) {
        Region region = Region.builder().code(regionCode).name("시도").build();
        RegionSubdivisions subdivision = RegionSubdivisions.builder().code(code).name(name).build();
        region.addSubdivision(subdivision);
        return subdivision;
    }
}
