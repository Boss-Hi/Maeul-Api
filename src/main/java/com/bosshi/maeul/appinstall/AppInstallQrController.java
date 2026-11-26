package com.bosshi.maeul.appinstall;

import com.bosshi.maeul.qr.QrImageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.regex.Pattern;

/** 운영자: 앱 설치 유입 QR. QR 이 가리키는 주소는 {APP_PUBLIC_BASE_URL}/app 으로 고정이다 */
@RestController
@RequestMapping("/api/admin/qr")
@PreAuthorize("hasRole('ADMIN')")
public class AppInstallQrController {

    private static final Pattern SRC = Pattern.compile("[A-Za-z0-9_-]{1,32}");

    private final QrImageService qrImageService;
    private final String publicBaseUrl;

    public AppInstallQrController(QrImageService qrImageService, @Value("${app-install.public-base-url:}") String publicBaseUrl) {
        this.qrImageService = qrImageService;
        this.publicBaseUrl = publicBaseUrl;
    }

    @GetMapping(value = "/app-install.png", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> png(@RequestParam(required = false) String src,
                                      @RequestParam(defaultValue = "" + QrImageService.DEFAULT_SIZE) int size) {
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(qrImageService.png(url(src), size));
    }

    @GetMapping(value = "/app-install.svg", produces = "image/svg+xml")
    public ResponseEntity<String> svg(@RequestParam(required = false) String src,
                                      @RequestParam(defaultValue = "" + QrImageService.DEFAULT_SIZE) int size) {
        return ResponseEntity.ok().contentType(MediaType.parseMediaType("image/svg+xml")).body(qrImageService.svg(url(src), size));
    }

    /** 도메인이 설정되지 않았으면 요청 호스트로 대신하지 않고 실패한다 (잘못된 도메인이 인쇄되지 않게) */
    String url(String src) {
        if (!publicBaseUrl.startsWith("https://") && !publicBaseUrl.startsWith("http://")) {
            throw new IllegalArgumentException("APP_PUBLIC_BASE_URL 이 설정되지 않았습니다.");
        }
        String base = publicBaseUrl.endsWith("/") ? publicBaseUrl.substring(0, publicBaseUrl.length() - 1) : publicBaseUrl;
        if (src == null || src.isEmpty()) {
            return base + "/app";
        }
        if (!SRC.matcher(src).matches()) {
            throw new IllegalArgumentException("src 는 영문, 숫자, -, _ 최대 32자여야 합니다.");
        }
        return base + "/app?src=" + src;
    }
}
