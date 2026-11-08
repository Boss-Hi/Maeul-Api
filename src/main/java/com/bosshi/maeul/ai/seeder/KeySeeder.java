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
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
@Order(1)
public class KeySeeder implements CommandLineRunner {
    private final AiApiKeyRepository repository;

    // 소스 코드에 키를 직접 적지 않고 설정 파일에서 읽어옵니다.
    @Value("${ai.gemini.api-key}")
    private String key;

    @Value("${ai.claude.api-key:}")
    private String claudeKey;

    @Override
    @Transactional
    public void run(String... args) {
        seedGemini();
        seedClaude();
    }

    /**
     * CLAUDE 키가 하나도 없고 설정 키가 있으면 1회 등록한다.
     */
    private void seedClaude() {
        if (!StringUtils.hasText(claudeKey) || repository.existsByProviderType(AiProviderType.CLAUDE)) {
            return;
        }

        repository.save(AiApiKey.builder()
                .apiKey(claudeKey)
                .providerType(AiProviderType.CLAUDE)
                .active(true)
                .name("Claude API Key")
                .build());
    }

    private void seedGemini() {
        if (repository.existsByProviderType(AiProviderType.GEMINI)) {
            return;
        }

        repository.save(AiApiKey.builder()
                .apiKey(key)
                .providerType(AiProviderType.GEMINI)
                .active(true)
                .name("Gemini API sub Key")
                .build());

        repository.save(AiApiKey.builder()
                .apiKey("gamcO/byrM9sXVKSzn6mqk6i9SRYpWt1fDfgKID5fKe58mhz/M5pQ+pvv7wZiVqIRLeHVWrS0Z/JiBg08hBI52pP8Sh8qxwtsASKBwdtZuOa")
                .providerType(AiProviderType.GEMINI)
                .active(true)
                .name("Gemini API sub Key")
                .build());
    }
}
