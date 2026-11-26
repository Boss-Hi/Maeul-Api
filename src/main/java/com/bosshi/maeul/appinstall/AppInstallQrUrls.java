package com.bosshi.maeul.appinstall;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.modulith.NamedInterface;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/** 앱 설치 QR 이 가리키는 주소. 고정 경로 {APP_PUBLIC_BASE_URL}/app 에 유입 출처(src)만 붙인다 */
@NamedInterface
@Component
public class AppInstallQrUrls {

    private static final Pattern SRC = Pattern.compile("[A-Za-z0-9_-]{1,32}");

    private final String publicBaseUrl;

    public AppInstallQrUrls(@Value("${app-install.public-base-url:}") String publicBaseUrl) {
        this.publicBaseUrl = publicBaseUrl;
    }

    /** 도메인이 설정되지 않았으면 요청 호스트로 대신하지 않고 실패한다 (잘못된 도메인이 인쇄되지 않게) */
    public String url(String src) {
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
