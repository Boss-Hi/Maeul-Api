package com.bosshi.maeul.ai.service;

import com.bosshi.maeul.ai.config.GeminiConfig;
import com.bosshi.maeul.ai.entity.AiApiKey;
import com.bosshi.maeul.ai.repository.GeminiApiKeyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GeminiServiceTest {

    @Mock
    private GeminiConfig config;

    @Mock
    private GeminiApiKeyRepository geminiApiKeyRepository;

    @InjectMocks
    private GeminiService geminiService;

    @Test
    void getApiKeyShouldReturnConfigKeyWhenNoActiveKeysInDatabase() {
        // given
        when(geminiApiKeyRepository.findNextApiKey()).thenReturn(Optional.empty());
        when(config.getApiKey()).thenReturn("config-api-key");

        // when
        String apiKey = geminiService.getApiKey();

        // then
        assertThat(apiKey).isEqualTo("config-api-key");
        verify(geminiApiKeyRepository, never()).save(any(AiApiKey.class));
    }

    @Test
    void getApiKeyShouldReturnDatabaseKeyAndUpdateLastUsedAtWhenActiveKeyExists() {
        // given
        AiApiKey mockKey = AiApiKey.builder()
                .id(1L)
                .apiKey("db-api-key-1")
                .active(true)
                .build();

        when(geminiApiKeyRepository.findNextApiKey()).thenReturn(Optional.of(mockKey));

        // when
        String apiKey = geminiService.getApiKey();

        // then
        assertThat(apiKey).isEqualTo("db-api-key-1");
        assertThat(mockKey.getLastUsedAt()).isNotNull();
        verify(geminiApiKeyRepository).save(mockKey);
    }
}
