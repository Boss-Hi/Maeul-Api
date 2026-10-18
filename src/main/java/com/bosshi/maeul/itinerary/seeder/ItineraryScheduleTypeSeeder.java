package com.bosshi.maeul.itinerary.seeder;

import com.bosshi.maeul.itinerary.entity.ItineraryScheduleType;
import com.bosshi.maeul.itinerary.repository.ItineraryScheduleTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Order(2)
public class ItineraryScheduleTypeSeeder implements CommandLineRunner {

    private final ItineraryScheduleTypeRepository repository;

    @Override
    @Transactional
    public void run(String... args) {
        if (repository.count() > 0) {
            return;
        }

        List<ItineraryScheduleType> types = List.of(
                ItineraryScheduleType.builder()
                        .code("TOUR")
                        .name("관광지 연동")
                        .description("한국관광공사 API contentId와 연동되는 일정 항목입니다.")
                        .build(),
                ItineraryScheduleType.builder()
                        .code("NORMAL")
                        .name("직접 입력")
                        .description("title/description을 직접 입력하는 일정 항목입니다.")
                        .build()
        );

        repository.saveAll(types);
    }
}

