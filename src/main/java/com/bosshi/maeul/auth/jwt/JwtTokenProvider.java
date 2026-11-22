package com.bosshi.maeul.auth.jwt;

import com.bosshi.maeul.auth.type.AuthType;
import com.bosshi.maeul.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.modulith.NamedInterface;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.Date;
import java.util.Map;

/**
 * JWT 토큰 생성, 파싱, 검증을 담당하는 컴포넌트.
 * application.yml의 jwt.secret(Base64 인코딩)으로 서명한다. 수명은 auth.token.access-ttl 이다.
 */
@NamedInterface
@Component
public class JwtTokenProvider {
    @Value("${jwt.secret}")
    private String secretKey;

    /**
     * 액세스 토큰 (ADR 0015). subject 는 이메일 로그인이면 이메일, 카카오·Apple 이면 그 수단의 사용자 ID 다.
     * JwtAuthenticationFilter 가 (authType, subject)로 사용자를 찾는다.
     */
    public String generateAccessToken(User user, Duration ttl) {
        AuthType authType = user.getAuthType() == null ? AuthType.BASIC : AuthType.valueOf(user.getAuthType());
        String authId = user.getAuthId() == null ? "" : user.getAuthId();
        Date now = new Date();
        return Jwts.builder()
                .subject(authType == AuthType.BASIC ? user.getEmail() : authId)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + ttl.toMillis()))
                .claims(Map.of(
                        "userId", user.getId(),
                        "email", user.getEmail(),
                        "authId", authId,
                        "authType", authType.name()))
                .signWith(getSigningKey())
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

    public String getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.get("userId", Long.class).toString();
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
