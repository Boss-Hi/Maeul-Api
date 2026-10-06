package com.bosshi.maeul.auth.jwt;

import com.bosshi.maeul.auth.type.AuthType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.modulith.NamedInterface;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

/**
 * JWT 토큰 생성, 파싱, 검증을 담당하는 컴포넌트.
 * application.yml의 jwt.secret(Base64 인코딩)과 jwt.expiration(ms)을 사용한다.
 */
@NamedInterface
@Component
public class JwtTokenProvider {
    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long expirationMs;

    /**
     * 이메일을 subject로 담은 서명된 JWT를 생성한다.
     */
    public String generateToken(String email) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(email)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();
    }

    public String generateToken(OAuth2User oAuth2User) {
        // 카카오 사용자 식별자 추출 (Kakao 고유 ID 또는 이메일)
        String providerId = String.valueOf(oAuth2User.getAttributes().get("id"));

        AuthType authType = AuthType.BASIC;
        String email = null;

        Map<String, Object> kakaoAccount = (Map<String, Object>) oAuth2User.getAttributes().get("kakao_account");
        if(kakaoAccount != null) {
            authType = AuthType.KAKAO;
            email = providerId;
        } else {
            email = (String) oAuth2User.getAttributes().get("email");
        }

        Date now = new Date();
        Date validity = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .setSubject(providerId)
                .claims(Map.of("email", email, "authType", authType))
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 토큰의 payload에서 이메일(subject)을 추출한다.
     */
    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject();
    }

    /**
     * 토큰의 payload에서 authType을 추출한다. 없거나 유효하지 않으면 BASIC을 반환한다.
     */
    public AuthType getAuthTypeFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        String authTypeValue = claims.get("authType", String.class);
        if (authTypeValue == null || authTypeValue.isBlank()) {
            return AuthType.BASIC;
        }

        try {
            return AuthType.valueOf(authTypeValue.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return AuthType.BASIC;
        }
    }

    /**
     * 서명 및 만료 여부를 검증한다. 유효하면 true, 아니면 false.
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Base64 디코딩된 secret으로 HMAC-SHA 서명 키를 생성한다.
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
