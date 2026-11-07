package com.bosshi.maeul.ai.service;

import com.bosshi.maeul.ai.config.GeminiConfig;
import com.bosshi.maeul.ai.response.GeminiGenerateResponse;
import com.bosshi.maeul.common.exception.ServiceUnavailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@DisplayName("GeminiService 키 순환 테스트")
class GeminiServiceTest {

    private static final String INVALID_KEY_BODY = """
            {"error":{"code":400,"message":"API key not valid.","status":"INVALID_ARGUMENT",
             "details":[{"@type":"type.googleapis.com/google.rpc.ErrorInfo","reason":"API_KEY_INVALID"}]}}""";
    private static final String SUCCESS_BODY = """
            {"candidates":[{"content":{"parts":[{"text":"{}"}]}}]}""";

    private MockRestServiceServer server;
    private GeminiApiKeyProvider keyProvider;
    private GeminiService service;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        keyProvider = mock(GeminiApiKeyProvider.class);

        GeminiConfig config = new GeminiConfig();
        config.setBaseUrl("https://gemini.test");
        config.setModel("m");
        config.setTemperature(0.5);
        config.setMaxOutputTokens(100);

        service = new GeminiService(builder, config, keyProvider);
    }

    @Test
    @DisplayName("API_KEY_INVALID 이면 키를 비활성화하고 다음 키로 재시도한다")
    void deactivatesInvalidKeyAndRetries() {
        when(keyProvider.acquire()).thenReturn(
                Optional.of(new GeminiApiKeyProvider.UsableKey(1L, "A", "bad")),
                Optional.of(new GeminiApiKeyProvider.UsableKey(2L, "B", "good")));
        server.expect(queryParam("key", "bad"))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body(INVALID_KEY_BODY));
        server.expect(queryParam("key", "good"))
                .andRespond(withSuccess(SUCCESS_BODY, MediaType.APPLICATION_JSON));

        GeminiGenerateResponse response = service.generate("prompt");

        assertThat(response).isNotNull();
        verify(keyProvider).deactivate(1L, "API_KEY_INVALID");
        server.verify();
    }

    @Test
    @DisplayName("모든 키가 거부되면 503 예외를 던진다")
    void throwsServiceUnavailableWhenKeysExhausted() {
        when(keyProvider.acquire()).thenReturn(
                Optional.of(new GeminiApiKeyProvider.UsableKey(1L, "A", "bad")),
                Optional.empty());
        server.expect(queryParam("key", "bad"))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body(INVALID_KEY_BODY));

        assertThatThrownBy(() -> service.generate("prompt")).isInstanceOf(ServiceUnavailableException.class);
        verify(keyProvider).deactivate(1L, "API_KEY_INVALID");
    }

    @Test
    @DisplayName("같은 키가 다시 나오면 무한 재시도하지 않는다")
    void stopsWhenSameKeyReturnedAgain() {
        GeminiApiKeyProvider.UsableKey key = new GeminiApiKeyProvider.UsableKey(1L, "A", "bad");
        when(keyProvider.acquire()).thenReturn(Optional.of(key));
        server.expect(queryParam("key", "bad"))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body(INVALID_KEY_BODY));

        assertThatThrownBy(() -> service.generate("prompt")).isInstanceOf(ServiceUnavailableException.class);
    }

    @Test
    @DisplayName("429 는 키를 끄지 않고 그대로 실패한다")
    void doesNotDeactivateOnRateLimit() {
        when(keyProvider.acquire()).thenReturn(Optional.of(new GeminiApiKeyProvider.UsableKey(1L, "A", "k")));
        server.expect(queryParam("key", "k"))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS).contentType(MediaType.APPLICATION_JSON)
                        .body("{\"error\":{\"status\":\"RESOURCE_EXHAUSTED\"}}"));

        assertThatThrownBy(() -> service.generate("prompt")).isInstanceOf(HttpClientErrorException.TooManyRequests.class);
        verify(keyProvider, never()).deactivate(anyLong(), anyString());
    }

    @Test
    @DisplayName("403 PERMISSION_DENIED 이면 키를 비활성화한다")
    void deactivatesOnPermissionDenied() {
        when(keyProvider.acquire()).thenReturn(
                Optional.of(new GeminiApiKeyProvider.UsableKey(1L, "A", "denied")),
                Optional.empty());
        server.expect(queryParam("key", "denied"))
                .andRespond(withStatus(HttpStatus.FORBIDDEN).contentType(MediaType.APPLICATION_JSON)
                        .body("{\"error\":{\"code\":403,\"status\":\"PERMISSION_DENIED\"}}"));

        assertThatThrownBy(() -> service.generate("prompt")).isInstanceOf(ServiceUnavailableException.class);
        verify(keyProvider).deactivate(1L, "PERMISSION_DENIED");
    }

    @Test
    @DisplayName("로그용 URI 에서 API 키를 가린다")
    void masksApiKey() {
        URI uri = URI.create("https://gemini.test/v1/models/m:generateContent?key=secret123&x=1");
        assertThat(GeminiService.maskApiKey(uri))
                .isEqualTo("https://gemini.test/v1/models/m:generateContent?key=****&x=1")
                .doesNotContain("secret123");
    }
}
