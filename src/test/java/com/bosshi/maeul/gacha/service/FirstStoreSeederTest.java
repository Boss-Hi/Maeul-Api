package com.bosshi.maeul.gacha.service;

import com.bosshi.maeul.gacha.entity.OfflinePrizeKind;
import com.bosshi.maeul.gacha.entity.Store;
import com.bosshi.maeul.gacha.repository.OfflinePrizeKindRepository;
import com.bosshi.maeul.gacha.repository.StoreRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("FirstStoreSeeder 첫 매장과 상품 종류")
class FirstStoreSeederTest {

    private final StoreRepository storeRepository = mock(StoreRepository.class);
    private final OfflinePrizeKindRepository kindRepository = mock(OfflinePrizeKindRepository.class);
    private final FirstStoreSeeder seeder = new FirstStoreSeeder(storeRepository, kindRepository);

    @Test
    @DisplayName("매장 1곳(병점구, 두 코드를 따로 저장)과 상품 종류 5개를 만든다")
    void createsStoreAndFiveKinds() {
        when(storeRepository.findFirstByNameAndDeletedAtIsNullOrderByIdAsc("더온전히 정조효공원점"))
                .thenReturn(java.util.Optional.of(Store.builder().id(9L).name("더온전히 정조효공원점").build()));

        seeder.run();

        ArgumentCaptor<Store> store = ArgumentCaptor.forClass(Store.class);
        verify(storeRepository).save(store.capture());
        assertThat(store.getValue().getName()).isEqualTo("더온전히 정조효공원점");
        assertThat(store.getValue().getRegionCode()).isEqualTo("41");
        assertThat(store.getValue().getSubdivisionCode()).isEqualTo("595");

        ArgumentCaptor<OfflinePrizeKind> kinds = ArgumentCaptor.forClass(OfflinePrizeKind.class);
        verify(kindRepository, times(5)).save(kinds.capture());
        assertThat(kinds.getAllValues()).extracting(OfflinePrizeKind::getStoreId).containsOnly(9L);
        assertThat(kinds.getAllValues()).extracting(OfflinePrizeKind::getName).containsExactly(
                "아메리카노 1잔", "물고기 친구 분양", "Maeul 병아리 스티커", "디저트 1개: 쌀 휘낭시에", "디저트 1개: 쌀 소금빵");
    }

    @Test
    @DisplayName("같은 이름이 이미 있으면(지워진 행 포함) 다시 만들지 않는다")
    void skipsExistingNames() {
        when(storeRepository.existsByName(anyString())).thenReturn(true);
        when(kindRepository.existsByName(anyString())).thenReturn(true);

        seeder.run();

        verify(storeRepository, never()).save(any());
        verify(kindRepository, never()).save(any());
    }
}
