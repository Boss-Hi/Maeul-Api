package com.bosshi.maeul.ai.listener;

import com.bosshi.maeul.ai.entity.AiApiKey;
import com.bosshi.maeul.crypt.AesCryptoConverter;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AiApiKeyEntityListener {
    private final AesCryptoConverter cryptoConverter;

    @PrePersist
    @PreUpdate
    public void encryptApiKey(AiApiKey entity) {
        if (entity.getApiKey() != null && !entity.getApiKey().isBlank()) {
            // 입력된 평문을 DB 암호화 포맷으로 변환
            String encryptedKey = cryptoConverter.convertToDatabaseColumn(entity.getApiKey());
            entity.setApiKey(encryptedKey);
        }
    }
}
