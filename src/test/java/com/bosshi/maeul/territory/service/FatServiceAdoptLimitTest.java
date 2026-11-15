package com.bosshi.maeul.territory.service;

import com.bosshi.maeul.territory.config.TerritoryProperties;
import com.bosshi.maeul.territory.entity.FatSetting;
import com.bosshi.maeul.territory.entity.KakaoLegalDongCode;
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
    private final KakaoLegalDongCodeRepository legalDongCodeRepository = mock(KakaoLegalDongCodeRepository.class);
    private final FatService service = new FatService(fatRepository, mock(FatLevelRepository.class), settingRepository,
            mock(TerritoryDailyStayRepository.class), legalDongCodeRepository, new TerritoryProperties(), new TimelineProperties());

    @BeforeEach
    void setUp() {
        when(legalDongCodeRepository.findById(1L)).thenReturn(Optional.of(
                KakaoLegalDongCode.builder().id(1L).code("11560").sidoName("서울특별시").sigunguName("영등포구").build()));
    }

    @Test
    @DisplayName("키우는 병아리가 DB 설정의 최대 마리 수에 이르면 분양을 거부하고 저장하지 않는다")
    void rejectsAtLimit() {
        when(settingRepository.findFirstByDeletedAtIsNullOrderByIdAsc())
                .thenReturn(Optional.of(FatSetting.builder().maxActiveFats(2).build()));
        when(fatRepository.countByUserIdAndGraduatedAtIsNull(7L)).thenReturn(2L);

        assertThatThrownBy(() -> service.adopt(7L, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("2마리");
        verify(fatRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("카카오 법정동 코드 표에 없는 시군구 ID 는 분양을 거부한다")
    void rejectsUnknownRegion() {
        when(legalDongCodeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.adopt(7L, 999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("존재하지 않는 시군구");
        verify(fatRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("설정 행이 없으면 분양을 막는다")
    void failsWithoutSetting() {
        when(settingRepository.findFirstByDeletedAtIsNullOrderByIdAsc()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.adopt(7L, 1L)).isInstanceOf(IllegalStateException.class);
        verify(fatRepository, never()).saveAndFlush(any());
    }
}
