package com.bosshi.maeul.ai.repository;

import com.bosshi.maeul.ai.entity.AiApiKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AiApiKeyRepository extends JpaRepository<AiApiKey, Long> {
}
