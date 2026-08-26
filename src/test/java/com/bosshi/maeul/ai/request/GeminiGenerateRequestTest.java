package com.bosshi.maeul.ai.request;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GeminiGenerateRequestTest {

    @Test
    void ofPromptBuildsUserContentAndGenerationConfig() {
        GeminiGenerateRequest request = GeminiGenerateRequest.ofPrompt("테스트 프롬프트", 0.4, 256);

        assertThat(request.getContents()).hasSize(1);
        assertThat(request.getContents().get(0).getRole()).isEqualTo("user");
        assertThat(request.getContents().get(0).getParts()).hasSize(1);
        assertThat(request.getContents().get(0).getParts().get(0).getText()).isEqualTo("테스트 프롬프트");

        assertThat(request.getGenerationConfig()).isNotNull();
        assertThat(request.getGenerationConfig().getTemperature()).isEqualTo(0.4);
        assertThat(request.getGenerationConfig().getMaxOutputTokens()).isEqualTo(256);
    }
}

