package com.bosshi.maeul.itinerary.seeder;

import com.bosshi.maeul.itinerary.repository.MissionVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Order(6)
public class MissionVerificationSeeder implements CommandLineRunner {

    private final MissionVerificationRepository repository;

    @Override
    @Transactional
    public void run(String... args) {
        if (repository.count() > 0) {
            return;
        }
    }
}
