package com.bosshi.maeul.ai.repository;

import com.bosshi.maeul.ai.entity.AiApiKey;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GeminiApiKeyRepository extends JpaRepository<AiApiKey, Long> {

    @Query("SELECT k FROM AiApiKey k WHERE k.active = true AND k.deletedAt IS NULL " +
           "ORDER BY CASE WHEN k.lastUsedAt IS NULL THEN 0 ELSE 1 END, k.lastUsedAt ASC, k.id ASC")
    List<AiApiKey> findActiveKeysOrderLastUsed(Pageable pageable);

    default Optional<AiApiKey> findNextApiKey() {
        List<AiApiKey> keys = findActiveKeysOrderLastUsed(Pageable.ofSize(1));
        return keys.isEmpty() ? Optional.empty() : Optional.of(keys.get(0));
    }
}
