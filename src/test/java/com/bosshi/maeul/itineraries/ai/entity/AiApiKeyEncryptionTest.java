package com.bosshi.maeul.itineraries.ai.entity;

import com.bosshi.maeul.itineraries.ai.type.AiProviderType;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(properties = {
        "app.encrypt.db-key=test-db-key",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DisplayName("AiApiKey 암호화 저장 테스트")
class AiApiKeyEncryptionTest {

    private static final String PLAIN_KEY = "AIza-plain-test-key";

    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("키는 암호화되어 저장되고, 다른 컬럼을 수정해도 중첩 암호화되지 않는다")
    void apiKeyIsNotReEncryptedOnUpdate() {
        AiApiKey key = AiApiKey.builder()
                .name("test").providerType(AiProviderType.GEMINI).apiKey(PLAIN_KEY)
                .build();
        em.persist(key);
        em.flush();
        String storedAfterInsert = rawApiKey(key.getId());
        assertThat(storedAfterInsert).isNotEqualTo(PLAIN_KEY);

        for (int i = 0; i < 5; i++) {
            key.setLastUsedAt(LocalDateTime.now().plusSeconds(i));
            em.flush();
        }
        key.setActive(false);
        em.flush();

        // 업데이트 시 새 IV 로 다시 암호화될 수는 있지만, 중첩 암호화로 길이가 늘어나서는 안 된다
        assertThat(rawApiKey(key.getId())).hasSameSizeAs(storedAfterInsert);

        em.clear();
        AiApiKey reloaded = em.find(AiApiKey.class, key.getId());
        assertThat(reloaded.getApiKey()).isEqualTo(PLAIN_KEY);
        assertThat(reloaded.isActive()).isFalse();
    }

    private String rawApiKey(Long id) {
        return (String) em.createNativeQuery("select api_key from ai_api_keys where id = :id")
                .setParameter("id", id)
                .getSingleResult();
    }
}
