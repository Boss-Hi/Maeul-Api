package com.bosshi.maeul.ai.service;

import com.bosshi.maeul.ai.repository.GeminiApiKeyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Gemini API 키의 선택과 상태 변경을 담당한다.
 * 호출 측 트랜잭션이 롤백되어도 키 상태(lastUsedAt, active)는 유지되도록 별도 트랜잭션으로 처리한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GeminiApiKeyProvider {
    private final GeminiApiKeyRepository repository;

    /**
     * 가장 오래전에 사용된 활성 키를 꺼내고 사용 시각을 기록한다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Optional<UsableKey> acquire() {
        return repository.findNextApiKey().map(key -> {
            key.setLastUsedAt(LocalDateTime.now());
            return new UsableKey(key.getId(), key.getName(), key.getApiKey());
        });
    }

    /**
     * Gemini 가 거부한 키를 비활성화한다. 다시 사용하려면 DB 에서 active=true 로 변경해야 한다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deactivate(Long keyId, String reason) {
        repository.findById(keyId).ifPresent(key -> {
            key.setActive(false);
            log.error("Gemini API key 비활성화 - id={}, name={}, reason={}. 유효한 키로 교체하거나 ai_api_keys.active 를 확인하세요.",
                    key.getId(), key.getName(), reason);
        });
    }

    @Transactional(readOnly = true)
    public boolean hasAvailableKey() {
        return repository.existsByActiveTrueAndDeletedAtIsNull();
    }

    public record UsableKey(Long id, String name, String value) {
    }
}
