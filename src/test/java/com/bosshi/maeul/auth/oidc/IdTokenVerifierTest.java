package com.bosshi.maeul.auth.oidc;

import com.bosshi.maeul.auth.config.AuthProperties;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ID 토큰 검증 테스트")
class IdTokenVerifierTest {

    private static final String KAKAO_ISS = "https://kauth.kakao.com";
    private static final String APPLE_ISS = "https://appleid.apple.com";
    private final KeyPair keys = generate();
    private final KeyPair otherKeys = generate();
    private final IdTokenVerifier verifier = new IdTokenVerifier(new AuthProperties(), Map.of(
            IdTokenProvider.KAKAO, decoder(KAKAO_ISS, List.of("native-key")),
            IdTokenProvider.APPLE, decoder(APPLE_ISS, List.of("com.bosshi.maeul"))));

    @Test
    @DisplayName("카카오: 서명·iss·aud·nonce 가 맞으면 회원번호와 닉네임을 돌려준다")
    void kakaoValid() {
        VerifiedIdentity identity = verifier.verify(IdTokenProvider.KAKAO,
                sign(keys, KAKAO_ISS, "native-key", "12345", "n-1", Instant.now().plusSeconds(600)), "n-1");

        assertThat(identity.subject()).isEqualTo("12345");
        assertThat(identity.nickname()).isEqualTo("홍길동");
    }

    @Test
    @DisplayName("Apple: 토큰의 nonce 는 원문의 SHA-256 hex 다")
    void appleHashedNonce() {
        String token = sign(keys, APPLE_ISS, "com.bosshi.maeul", "apple-sub", IdTokenVerifier.sha256("raw"), Instant.now().plusSeconds(600));

        assertThat(verifier.verify(IdTokenProvider.APPLE, token, "raw").subject()).isEqualTo("apple-sub");
        assertThatThrownBy(() -> verifier.verify(IdTokenProvider.APPLE,
                sign(keys, APPLE_ISS, "com.bosshi.maeul", "apple-sub", "raw", Instant.now().plusSeconds(600)), "raw"))
                .isInstanceOf(InvalidIdTokenException.class);
    }

    @Test
    @DisplayName("nonce, aud, iss, 서명, 만료 중 하나라도 틀리면 거부한다")
    void rejectsInvalid() {
        Instant later = Instant.now().plusSeconds(600);
        assertThatThrownBy(() -> verifier.verify(IdTokenProvider.KAKAO, sign(keys, KAKAO_ISS, "native-key", "1", "n", later), "other"))
                .isInstanceOf(InvalidIdTokenException.class);
        assertThatThrownBy(() -> verifier.verify(IdTokenProvider.KAKAO, sign(keys, KAKAO_ISS, "someone-else", "1", "n", later), "n"))
                .isInstanceOf(InvalidIdTokenException.class);
        assertThatThrownBy(() -> verifier.verify(IdTokenProvider.KAKAO, sign(keys, APPLE_ISS, "native-key", "1", "n", later), "n"))
                .isInstanceOf(InvalidIdTokenException.class);
        assertThatThrownBy(() -> verifier.verify(IdTokenProvider.KAKAO, sign(otherKeys, KAKAO_ISS, "native-key", "1", "n", later), "n"))
                .isInstanceOf(InvalidIdTokenException.class);
        assertThatThrownBy(() -> verifier.verify(IdTokenProvider.KAKAO,
                sign(keys, KAKAO_ISS, "native-key", "1", "n", Instant.now().minusSeconds(600)), "n"))
                .isInstanceOf(InvalidIdTokenException.class);
    }

    private NimbusJwtDecoder decoder(String issuer, List<String> audiences) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey((RSAPublicKey) keys.getPublic()).build();
        decoder.setJwtValidator(IdTokenVerifier.validator(issuer, audiences));
        return decoder;
    }

    private static String sign(KeyPair keyPair, String issuer, String audience, String subject, String nonce, Instant expiresAt) {
        try {
            SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), new JWTClaimsSet.Builder()
                    .issuer(issuer).audience(audience).subject(subject).claim("nonce", nonce).claim("nickname", "홍길동")
                    .issueTime(new Date()).expirationTime(Date.from(expiresAt)).build());
            jwt.sign(new RSASSASigner(keyPair.getPrivate()));
            return jwt.serialize();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static KeyPair generate() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
