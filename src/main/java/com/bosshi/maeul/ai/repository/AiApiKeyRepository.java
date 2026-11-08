package com.bosshi.maeul.ai.repository;

import com.bosshi.maeul.ai.entity.AiApiKey;
import com.bosshi.maeul.ai.type.AiProviderType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AiApiKeyRepository extends JpaRepository<AiApiKey, Long> {

    @Query("SELECT k FROM AiApiKey k WHERE k.providerType = :providerType AND k.active = true AND k.deletedAt IS NULL " +
           "ORDER BY CASE WHEN k.lastUsedAt IS NULL THEN 0 ELSE 1 END, k.lastUsedAt ASC, k.id ASC")
    List<AiApiKey> findActiveKeysOrderLastUsed(@Param("providerType") AiProviderType providerType, Pageable pageable);

    boolean existsByProviderTypeAndActiveTrueAndDeletedAtIsNull(AiProviderType providerType);

    boolean existsByProviderType(AiProviderType providerType);

    /**
     * 해당 제공자의 활성 키 중 가장 오래전에 사용된 키
     */
    default Optional<AiApiKey> findNextApiKey(AiProviderType providerType) {
        List<AiApiKey> keys = findActiveKeysOrderLastUsed(providerType, Pageable.ofSize(1));
        return keys.isEmpty() ? Optional.empty() : Optional.of(keys.get(0));
    }
}
