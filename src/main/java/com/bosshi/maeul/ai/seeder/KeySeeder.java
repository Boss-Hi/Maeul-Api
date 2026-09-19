package com.bosshi.maeul.ai.seeder;

import com.bosshi.maeul.ai.entity.AiApiKey;
import com.bosshi.maeul.ai.repository.AiApiKeyRepository;
import com.bosshi.maeul.ai.type.AiProviderType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Order(1)
public class KeySeeder implements CommandLineRunner {
    private final AiApiKeyRepository repository;

    // 소스 코드에 키를 직접 적지 않고 설정 파일에서 읽어옵니다.
    @Value("${ai.gemini.api-key}")
    private String key;

    @Override
    @Transactional
    public void run(String... args) {
        if (repository.count() > 0) {
            return;
        }

        repository.save(AiApiKey.builder()
                .apiKey(key)
                .providerType(AiProviderType.GEMINI)
                .active(true)
                .name("Gemini API sub Key")
                .build());
    }
}
