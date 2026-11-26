package com.bosshi.maeul.appinstall;

import com.bosshi.maeul.qr.QrImageService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 운영자: 앱 설치 유입 QR. QR 이 가리키는 주소는 {APP_PUBLIC_BASE_URL}/app 으로 고정이다 */
@RestController
@RequestMapping("/api/admin/qr")
@PreAuthorize("hasRole('ADMIN')")
public class AppInstallQrController {

    private final QrImageService qrImageService;
    private final AppInstallQrUrls urls;

    public AppInstallQrController(QrImageService qrImageService, AppInstallQrUrls urls) {
        this.qrImageService = qrImageService;
        this.urls = urls;
    }

    @GetMapping(value = "/app-install.png", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> png(@RequestParam(required = false) String src,
                                      @RequestParam(defaultValue = "" + QrImageService.DEFAULT_SIZE) int size) {
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(qrImageService.png(urls.url(src), size));
    }

    @GetMapping(value = "/app-install.svg", produces = "image/svg+xml")
    public ResponseEntity<String> svg(@RequestParam(required = false) String src,
                                      @RequestParam(defaultValue = "" + QrImageService.DEFAULT_SIZE) int size) {
        return ResponseEntity.ok().contentType(MediaType.parseMediaType("image/svg+xml")).body(qrImageService.svg(urls.url(src), size));
    }
}
