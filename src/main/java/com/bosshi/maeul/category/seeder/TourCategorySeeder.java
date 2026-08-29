package com.bosshi.maeul.category.seeder;

import com.bosshi.maeul.category.repository.TourCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class TourCategorySeeder implements CommandLineRunner {

    private final TourCategoryRepository repository;

    @Override
    @Transactional
    public void run(String... args) {
        return;
    }
}
