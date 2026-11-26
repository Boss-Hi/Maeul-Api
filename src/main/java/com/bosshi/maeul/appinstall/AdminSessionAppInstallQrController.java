package com.bosshi.maeul.appinstall;

import com.bosshi.maeul.qr.QrImageService;
import com.kraftadmin.security.AdminSessionStore;
import com.kraftadmin.security.SessionConfig;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * KraftAdmin 로그인(세션 쿠키) 상태의 운영자가 브라우저 주소창으로 바로 여는 앱 설치 QR.
 * KraftAdmin 화면이 엔티티와 무관한 버튼을 지원하지 않아 /admin 아래 경로로 둔다 (JWT 용 /api/admin/qr 과 같은 내용).
 */
@RestController
public class AdminSessionAppInstallQrController {

    private final QrImageService qrImageService;
    private final AppInstallQrUrls urls;
    private final AdminSessionStore sessionStore;
    private final SessionConfig sessionConfig;

    public AdminSessionAppInstallQrController(QrImageService qrImageService, AppInstallQrUrls urls,
                                              AdminSessionStore sessionStore, SessionConfig sessionConfig) {
        this.qrImageService = qrImageService;
        this.urls = urls;
        this.sessionStore = sessionStore;
        this.sessionConfig = sessionConfig;
    }

    @GetMapping(value = "${kraftadmin.base-path:/admin}/qr/app-install.png", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> png(HttpServletRequest request, @RequestParam(required = false) String src,
                                      @RequestParam(defaultValue = "" + QrImageService.DEFAULT_SIZE) int size) {
        if (!loggedIn(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(qrImageService.png(urls.url(src), size));
    }

    @GetMapping(value = "${kraftadmin.base-path:/admin}/qr/app-install.svg", produces = "image/svg+xml")
    public ResponseEntity<String> svg(HttpServletRequest request, @RequestParam(required = false) String src,
                                      @RequestParam(defaultValue = "" + QrImageService.DEFAULT_SIZE) int size) {
        if (!loggedIn(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok().contentType(MediaType.parseMediaType("image/svg+xml")).body(qrImageService.svg(urls.url(src), size));
    }

    private boolean loggedIn(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return false;
        }
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(sessionConfig.getCookieName()) && sessionStore.get(cookie.getValue()) != null) {
                return true;
            }
        }
        return false;
    }
}
