package com.bosshi.maeul.ai;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AiRequest 이미지")
class AiRequestImagesTest {

    @Test
    @DisplayName("제공자를 제외한 요청도 이미지를 그대로 가진다")
    void excludingKeepsImages() {
        AiImage image = new AiImage(new byte[]{1}, "image/jpeg");
        AiRequest<String> request = AiRequest.withImages("p", List.of(image), String.class, null).excluding("claude");

        assertThat(request.images()).containsExactly(image);
        assertThat(request.excludedProviders()).containsExactly("claude");
        assertThat(AiRequest.of("p", String.class, null).images()).isEmpty();
    }
}
