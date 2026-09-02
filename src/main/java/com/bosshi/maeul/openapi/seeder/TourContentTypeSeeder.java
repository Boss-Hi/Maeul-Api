package com.bosshi.maeul.openapi.seeder;

import com.bosshi.maeul.openapi.entity.TourCategoryType;
import com.bosshi.maeul.openapi.repository.TourCategoryTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Order(1)
public class TourContentTypeSeeder implements CommandLineRunner {

    private final TourCategoryTypeRepository repository;

    @Override
    @Transactional
    public void run(String... args) {
        if (repository.count() > 0) {
            return;
        }

        // idempotent upsert: insert or update known content types
        List<TourCategoryType> types = List.of(
            TourCategoryType.builder().contentTypeId("12").contentTypeIdMultiLang("76").name("관광지").active(true).build(),
            TourCategoryType.builder().contentTypeId("14").contentTypeIdMultiLang("78").name("문화시설").active(true).build(),
            TourCategoryType.builder().contentTypeId("15").contentTypeIdMultiLang("85").name("축제/공연/행사").active(true).build(),
            TourCategoryType.builder().contentTypeId("25").contentTypeIdMultiLang(null).name("여행코스").active(true).build(),
            TourCategoryType.builder().contentTypeId("28").contentTypeIdMultiLang("75").name("레포츠").active(true).build(),
            TourCategoryType.builder().contentTypeId("32").contentTypeIdMultiLang("80").name("숙박").active(true).build(),
            TourCategoryType.builder().contentTypeId("38").contentTypeIdMultiLang("79").name("쇼핑").active(true).build(),
            TourCategoryType.builder().contentTypeId("39").contentTypeIdMultiLang("82").name("음식점").active(true).build()
        );
        repository.saveAll(types);
    }
}
