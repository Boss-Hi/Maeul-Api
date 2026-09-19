package com.bosshi.maeul.itinerary.seeder;

import com.bosshi.maeul.itinerary.entity.MissionTrigger;
import com.bosshi.maeul.itinerary.repository.MissionTriggerRepository;
import com.bosshi.maeul.itinerary.type.MissionTriggerType;
import com.bosshi.maeul.openapi.repository.TourCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Order(5)
public class MissionTriggerSeeder implements CommandLineRunner {

    private final MissionTriggerRepository repository;
    private final TourCategoryRepository tourCategoryRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (repository.count() > 0) {
            return;
        }

        ArrayList<MissionTrigger> triggers = tourCategoryRepository.findAll().stream()
                .map(category -> MissionTrigger.builder()
                        .type(MissionTriggerType.CATEGORY)
                        .name(category.getName() + "(" + category.getCode() + ")")
                        .value(category.getCode())
                        .description(category.getName() + "(" + category.getCode() + ") 카테고리에 발생")
                        .build())
                .collect(Collectors.toCollection(ArrayList::new));

        triggers.add(MissionTrigger.builder()
                .type(MissionTriggerType.FIRST)
                .name("공통(전체 테마) — 온보딩")
                .value("FIRST")
                .description("\"첫 여행 시 자동 매칭\n" +
                        "테마·장소 구분 없이 사용자가 앱에서 처음으로 아무 관광지 POI에나 반경 100m 이내로 진입하는 순간 1회 자동 인증. 이후에는 이미 완료된 미션이라 재노출되지 않고, 카운트도 늘지 않음(상한 1).\"")
                .build());

        repository.saveAll(triggers);
    }
}
