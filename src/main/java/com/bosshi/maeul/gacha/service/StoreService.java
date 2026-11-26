package com.bosshi.maeul.gacha.service;

import com.bosshi.maeul.gacha.entity.OfflineGachaLog;
import com.bosshi.maeul.gacha.entity.Store;
import com.bosshi.maeul.gacha.entity.StoreOwner;
import com.bosshi.maeul.gacha.repository.OfflineGachaLogRepository;
import com.bosshi.maeul.gacha.repository.StoreOwnerRepository;
import com.bosshi.maeul.gacha.repository.StoreRepository;
import com.bosshi.maeul.itineraries.openapi.repository.LDongSigunguRepository;
import com.bosshi.maeul.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** 매장과 사장님 (ADR 0016). 매장은 운영자가 등록하고, 사장님은 매장 하나에만 속한다 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;
    private final StoreOwnerRepository ownerRepository;
    private final OfflineGachaLogRepository logRepository;
    private final LDongSigunguRepository sigunguRepository;
    private final UserRepository userRepository;

    /** 시도 코드와 시군구 코드는 region_subdivisions 의 region_code 와 code 를 각각 받는다 */
    @Transactional
    public Store register(String name, String regionCode, String subdivisionCode) {
        if (sigunguRepository.findWithRegionByRegionCodeAndCode(regionCode, subdivisionCode).isEmpty()) {
            throw new IllegalArgumentException("시군구를 찾을 수 없습니다: " + regionCode + ", " + subdivisionCode);
        }
        return storeRepository.save(Store.builder().name(name).regionCode(regionCode).subdivisionCode(subdivisionCode).build());
    }

    public List<Store> stores() {
        return storeRepository.findByDeletedAtIsNullOrderByIdAsc();
    }

    @Transactional
    public StoreOwner addOwner(Long storeId, Long userId) {
        if (!storeRepository.existsById(storeId)) {
            throw new IllegalArgumentException("매장을 찾을 수 없습니다: " + storeId);
        }
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId);
        }
        if (ownerRepository.findByUserIdAndDeletedAtIsNull(userId).isPresent()) {
            throw new IllegalArgumentException("이미 매장에 연결된 사장님입니다: " + userId);
        }
        return ownerRepository.save(StoreOwner.builder().storeId(storeId).userId(userId).build());
    }

    public List<StoreOwner> owners(Long storeId) {
        return ownerRepository.findByStoreIdAndDeletedAtIsNull(storeId);
    }

    /** 사장님이 속한 매장. 매장에 연결되지 않은 사용자는 스캔할 수 없다 */
    public Store requireStoreOf(Long ownerUserId) {
        StoreOwner owner = ownerRepository.findByUserIdAndDeletedAtIsNull(ownerUserId)
                .orElseThrow(() -> new IllegalArgumentException("매장에 연결된 사장님이 아닙니다."));
        return storeRepository.findById(owner.getStoreId())
                .filter(store -> !store.isDeleted())
                .orElseThrow(() -> new IllegalArgumentException("매장을 찾을 수 없습니다: " + owner.getStoreId()));
    }

    /** 사장님 자기 매장의 이력만 (최근 순) */
    public Page<OfflineGachaLog> history(Long ownerUserId, Pageable pageable) {
        return logRepository.findByStoreIdOrderByIdDesc(requireStoreOf(ownerUserId).getId(), pageable);
    }
}
