package com.bosshi.maeul.openapi.seeder;

import com.bosshi.maeul.openapi.dto.ldongCode2ListtDTO;
import com.bosshi.maeul.openapi.entity.LDongRegion;
import com.bosshi.maeul.openapi.entity.LDongSigungu;
import com.bosshi.maeul.openapi.repository.LDongRegionRepository;
import com.bosshi.maeul.openapi.service.OpenApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Order(4)
public class LDongSeeder implements CommandLineRunner {

    private final LDongRegionRepository repository;
    private final OpenApiService openApiService;

    @Override
    @Transactional
    public void run(String... args) {
        if (repository.count() > 0) {
            return;
        }

        var items = openApiService.getLdongCode2(
                        ldongCode2ListtDTO.builder()
                                .numOfRows(300)
                                .build()
                )
                .getResponse()
                .getBody()
                .getItems()
                .getItem();

        if (items == null || items.isEmpty()) {
            return;
        }

        // 시/도 코드(lDongRegnCd)를 기준으로 그룹화하여 부모-자식 객체 구성
        Map<String, LDongRegion> regionMap = new LinkedHashMap<>();

        for (var item : items) {
            String regionCode = item.getLDongRegnCd();
            String regionName = item.getLDongRegnNm();
            String sigunguCode = item.getLDongSignguCd();
            String sigunguName = item.getLDongSignguNm();
            Integer rnum = item.getRnum();

            // 부모 LDongRegion이 Map에 없으면 생성
            LDongRegion region = regionMap.computeIfAbsent(regionCode, code ->
                    LDongRegion.builder()
                            .code(code)
                            .name(regionName)
                            .build()
            );

            // 자식 LDongSigungu 생성 및 연관관계 편의 메서드로 연결
            LDongSigungu sigungu = LDongSigungu.builder()
                    .code(sigunguCode)
                    .name(sigunguName)
                    .rnum(rnum)
                    .build();

            region.addSigungu(sigungu);
        }

        // CascadeType.ALL 설정으로 부모만 saveAll() 호출하면 자식 시/군/구까지 영속화
        repository.saveAll(new ArrayList<>(regionMap.values()));
    }

    private Double parseDoubleSafely(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Double.valueOf(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
