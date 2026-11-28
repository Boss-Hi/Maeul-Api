package com.bosshi.maeul.itineraries.itinerary.photomission;

import com.bosshi.maeul.ai.AiImage;
import com.bosshi.maeul.common.exception.ServiceUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 기준 사진을 URL 에서 내려받는다. 서버가 임의 주소로 요청을 보내지 않도록 https 와 허용 호스트 목록으로 제한하고,
 * 리다이렉트는 따라가지 않는다.
 */
@Slf4j
@Component
public class ReferenceImageFetcher {

    private static final int MAX_BYTES = 5 * 1024 * 1024;

    private final Set<String> allowedHosts;
    private final HttpClient http = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NEVER)
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    public ReferenceImageFetcher(@Value("${photo-mission.allowed-image-hosts:}") String allowedHosts) {
        this.allowedHosts = Arrays.stream(allowedHosts.split(","))
                .map(String::trim)
                .filter(host -> !host.isEmpty())
                .map(host -> host.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
    }

    AiImage fetch(String url) {
        URI uri;
        try {
            uri = URI.create(url);
        } catch (IllegalArgumentException e) {
            throw unavailable("기준 사진 주소가 올바르지 않습니다: " + url, e);
        }
        if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null
                || !allowedHosts.contains(uri.getHost().toLowerCase(Locale.ROOT))) {
            throw unavailable("허용되지 않은 기준 사진 주소입니다: " + url, null);
        }
        try {
            HttpResponse<InputStream> response = http.send(
                    HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(10)).GET().build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            try (InputStream body = response.body()) {
                if (response.statusCode() != 200) {
                    throw unavailable("기준 사진을 받지 못했습니다: status=" + response.statusCode() + ", " + url, null);
                }
                byte[] data = body.readNBytes(MAX_BYTES + 1);
                String mime = PhotoMissionRules.detectMime(data);
                if (data.length > MAX_BYTES || mime == null) {
                    throw unavailable("기준 사진이 JPEG, PNG, WebP 가 아니거나 너무 큽니다: " + url, null);
                }
                return new AiImage(data, mime);
            }
        } catch (IOException e) {
            throw unavailable("기준 사진을 받지 못했습니다: " + url, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw unavailable("기준 사진 요청이 중단되었습니다: " + url, e);
        }
    }

    private static ServiceUnavailableException unavailable(String logMessage, Exception cause) {
        log.error(logMessage, cause);
        return new ServiceUnavailableException(PhotoMissionRules.UNAVAILABLE_MESSAGE, cause);
    }
}
