package com.bosshi.maeul.appinstall;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 앱 설치 페이지. QR 이 가리키는 고정 주소이며, 스토어로 바로 보내지 않고 이 웹을 거쳐 기기에 맞는 스토어로 보낸다.
 * 이미 설치한 사용자에게 앱을 열어 주는 딥링크는 범위 밖이다.
 */
@Slf4j
@RestController
public class AppInstallController {

    private final String androidUrl;
    private final String iosUrl;

    public AppInstallController(@Value("${app-install.android-url:}") String androidUrl,
                                @Value("${app-install.ios-url:}") String iosUrl) {
        this.androidUrl = httpUrlOrNull(androidUrl);
        this.iosUrl = httpUrlOrNull(iosUrl);
    }

    /** @param src 유입 출처(예: 매장 이름). 로그로만 남긴다 */
    @GetMapping(value = "/app", produces = MediaType.TEXT_HTML_VALUE + ";charset=UTF-8")
    public ResponseEntity<String> page(HttpServletRequest request, @RequestParam(required = false) String src) {
        String userAgent = request.getHeader(HttpHeaders.USER_AGENT);
        InstallTarget target = InstallPageResolver.resolve(userAgent, androidUrl, iosUrl);
        log.info("앱 설치 페이지: kind={}, src={}", target.kind(), src == null ? null : src.replaceAll("[\\r\\n]", "_"));
        String pageUrl = request.getRequestURL().toString();
        String html = InstallPageRenderer.render(target, androidUrl, iosUrl, pageUrl, InstallPageResolver.isAndroidKakaoTalk(userAgent));
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(html);
    }

    /** 스토어 링크는 http(s) 만 쓴다. 비어 있으면 그 플랫폼은 준비 중이다 */
    static String httpUrlOrNull(String url) {
        return url != null && (url.startsWith("https://") || url.startsWith("http://")) ? url : null;
    }
}
