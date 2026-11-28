package com.bosshi.maeul.gacha.service;

import com.bosshi.maeul.gacha.entity.OfflinePrizeKind;
import com.bosshi.maeul.gacha.entity.Store;
import com.bosshi.maeul.gacha.repository.OfflinePrizeKindRepository;
import com.bosshi.maeul.gacha.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 첫 오프라인 매장(더온전히 정조효공원점)과 가챠권으로 줄 수 있는 상품 종류를 등록한다.
 * 같은 이름의 행이 지워진 것까지 포함해 하나라도 있으면 다시 만들지 않아, 운영자가 바꾸거나 지운 것을 되살리지 않는다.
 * 상품 종류는 이 매장에서만 지급할 수 있다. 상품 QR 과 사장님 계정은 만들지 않는다 (운영이 발급·연결한다).
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Order(30)
public class FirstStoreSeeder implements CommandLineRunner {

    static final String STORE_NAME = "더온전히 정조효공원점";
    /** 경기도 화성시 병점구 (region_subdivisions.region_code, code). 이어 붙이지 않는다 */
    static final String STORE_REGION_CODE = "41";
    static final String STORE_SUBDIVISION_CODE = "595";

    static final List<String> PRIZE_KIND_NAMES = List.of(
            "아메리카노 1잔",
            "물고기 친구 분양",
            "Maeul 병아리 스티커",
            "디저트 1개: 쌀 휘낭시에",
            "디저트 1개: 쌀 소금빵");

    private final StoreRepository storeRepository;
    private final OfflinePrizeKindRepository kindRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (!storeRepository.existsByName(STORE_NAME)) {
            storeRepository.save(Store.builder()
                    .name(STORE_NAME)
                    .regionCode(STORE_REGION_CODE)
                    .subdivisionCode(STORE_SUBDIVISION_CODE)
                    .build());
            log.info("[FirstStoreSeeder] 매장 생성: {}", STORE_NAME);
        }
        // 매장이 지워졌으면 전용 매장 없이 만들지 않고 건너뛴다 (되살리지 않는 규칙과 같다)
        Long storeId = storeRepository.findFirstByNameAndDeletedAtIsNullOrderByIdAsc(STORE_NAME).map(Store::getId).orElse(null);
        if (storeId == null) {
            return;
        }
        for (String name : PRIZE_KIND_NAMES) {
            if (!kindRepository.existsByName(name)) {
                kindRepository.save(OfflinePrizeKind.builder().name(name).storeId(storeId).build());
                log.info("[FirstStoreSeeder] 상품 종류 생성: {}", name);
            }
        }
    }
}
