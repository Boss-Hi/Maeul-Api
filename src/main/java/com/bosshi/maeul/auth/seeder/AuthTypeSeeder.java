package com.bosshi.maeul.auth.seeder;

import com.bosshi.maeul.auth.entity.AuthType;
import com.bosshi.maeul.auth.repository.AuthTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Order(1)
public class AuthTypeSeeder implements CommandLineRunner {

    private final AuthTypeRepository repository;

    @Override
    @Transactional
    public void run(String... args) {
        if (repository.count() > 0) {
            return;
        }

        List<AuthType> authTypes = List.of(
                AuthType.builder()
                        .code("BASIC")
                        .name("기본 인증")
                        .description("기본 인증 유형입니다.")
                        .build(),
                AuthType.builder()
                        .code("KAKAO")
                        .name("Kakao")
                        .description("Kakao 인증 유형입니다.")
                        .build()
        );

        repository.saveAll(authTypes);
    }
}
