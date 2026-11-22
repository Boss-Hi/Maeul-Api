package com.bosshi.maeul.auth.oidc;

import com.bosshi.maeul.auth.config.AuthProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.EnumMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

/**
 * 카카오·Apple OIDC ID 토큰 검증 (ADR 0015). 제공자 공개키(JWKS)로 서명을 확인하고 iss, aud, 만료, nonce 를 본다.
 * nonce: 앱은 원문을 보낸다. 카카오는 토큰에 원문이, Apple 은 SHA-256 hex 가 들어 있다(sign_in_with_apple 이 해시를 넘긴다).
 */
@Component
public class IdTokenVerifier {

    private final AuthProperties properties;
    private final Map<IdTokenProvider, JwtDecoder> decoders = new EnumMap<>(IdTokenProvider.class);

    @Autowired
    public IdTokenVerifier(AuthProperties properties) {
        this.properties = properties;
    }

    IdTokenVerifier(AuthProperties properties, Map<IdTokenProvider, JwtDecoder> decoders) {
        this.properties = properties;
        this.decoders.putAll(decoders);
    }

    public VerifiedIdentity verify(IdTokenProvider provider, String idToken, String rawNonce) {
        if (rawNonce == null || rawNonce.isBlank()) {
            throw new InvalidIdTokenException("nonce 가 필요합니다.");
        }
        Jwt jwt;
        try {
            jwt = decoder(provider).decode(idToken);
        } catch (JwtException e) {
            throw new InvalidIdTokenException("유효하지 않은 ID 토큰입니다: " + e.getMessage(), e);
        }
        String expectedNonce = provider == IdTokenProvider.APPLE ? sha256(rawNonce) : rawNonce;
        if (!expectedNonce.equals(jwt.getClaimAsString("nonce"))) {
            throw new InvalidIdTokenException("nonce 가 일치하지 않습니다.");
        }
        if (jwt.getSubject() == null || jwt.getSubject().isBlank()) {
            throw new InvalidIdTokenException("ID 토큰에 사용자 ID(sub)가 없습니다.");
        }
        return new VerifiedIdentity(provider, jwt.getSubject(), jwt.getClaimAsString("nickname"));
    }

    private synchronized JwtDecoder decoder(IdTokenProvider provider) {
        return decoders.computeIfAbsent(provider, key -> {
            AuthProperties.Provider config = config(key);
            List<String> audiences = config.getAudiences().stream().filter(value -> value != null && !value.isBlank()).toList();
            if (audiences.isEmpty()) {
                throw new IllegalStateException(key + " ID 토큰의 aud(앱 키) 설정이 없습니다.");
            }
            NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(config.getJwkSetUri()).build();
            decoder.setJwtValidator(validator(config.getIssuer(), audiences));
            return decoder;
        });
    }

    static OAuth2TokenValidator<Jwt> validator(String issuer, List<String> audiences) {
        OAuth2TokenValidator<Jwt> audience = jwt -> jwt.getAudience() != null && jwt.getAudience().stream().anyMatch(audiences::contains)
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "aud 가 우리 앱이 아닙니다.", null));
        return new DelegatingOAuth2TokenValidator<>(JwtValidators.createDefaultWithIssuer(issuer), audience);
    }

    private AuthProperties.Provider config(IdTokenProvider provider) {
        return switch (provider) {
            case KAKAO -> properties.getKakao();
            case APPLE -> properties.getApple();
        };
    }

    static String sha256(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
