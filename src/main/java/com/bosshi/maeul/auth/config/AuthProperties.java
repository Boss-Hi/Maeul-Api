package com.bosshi.maeul.auth.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * 로그인 유지와 앱 로그인 설정 (ADR 0015)
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "auth")
public class AuthProperties {
    private Token token = new Token();
    private Provider kakao = new Provider("https://kauth.kakao.com", "https://kauth.kakao.com/.well-known/jwks.json");
    private Provider apple = new Provider("https://appleid.apple.com", "https://appleid.apple.com/auth/keys");

    /** 카카오 연결 끊기 콜백의 Authorization 헤더(KakaoAK {어드민 키})를 확인하는 어드민 키 */
    private String kakaoAdminKey = "";

    /** 네이티브 앱의 WebView 가 User-Agent 에 붙이는 표식. 이 표식이 있는 웹 OAuth 호출만 1회용 코드 방식을 쓴다 */
    private String nativeAppUserAgent = "MaeulApp";

    @Getter
    @Setter
    public static class Token {
        private Duration accessTtl = Duration.ofHours(1);
        private Duration refreshTtl = Duration.ofDays(60);
        /** 웹 OAuth 로그인 뒤 URL 로 넘기는 1회용 코드의 수명 */
        private Duration loginCodeTtl = Duration.ofMinutes(1);
        /** 웹 브라우저 OAuth 로그인이 URL 로 바로 주는 액세스 토큰의 수명 (리프레시 토큰이 없으므로 기존처럼 길게) */
        private Duration webAccessTtl = Duration.ofHours(10);
    }

    @Getter
    @Setter
    public static class Provider {
        private String issuer;
        private String jwkSetUri;
        /** ID 토큰 aud 로 받아들이는 값. 카카오: 네이티브 앱 키·REST API 키, Apple: 번들 ID·Services ID */
        private List<String> audiences = new ArrayList<>();

        public Provider() {
        }

        Provider(String issuer, String jwkSetUri) {
            this.issuer = issuer;
            this.jwkSetUri = jwkSetUri;
        }
    }
}
