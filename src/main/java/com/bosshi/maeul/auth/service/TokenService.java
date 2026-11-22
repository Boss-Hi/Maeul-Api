package com.bosshi.maeul.auth.service;

import com.bosshi.maeul.auth.config.AuthProperties;
import com.bosshi.maeul.auth.entity.LoginCode;
import com.bosshi.maeul.auth.entity.RefreshToken;
import com.bosshi.maeul.auth.jwt.JwtTokenProvider;
import com.bosshi.maeul.auth.repository.LoginCodeRepository;
import com.bosshi.maeul.auth.repository.RefreshTokenRepository;
import com.bosshi.maeul.user.entity.User;
import com.bosshi.maeul.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.NamedInterface;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 액세스 토큰과 리프레시 토큰 발급·회전·폐기 (ADR 0015).
 * 리프레시 토큰은 기기 로그인(family)마다 따로 있고, 쓸 때마다 새것으로 바뀐다.
 * 이미 쓴 리프레시 토큰이 다시 오면 탈취로 보고 그 family 를 모두 폐기한다.
 */
@Slf4j
@NamedInterface
@Service
@RequiredArgsConstructor
@Transactional(noRollbackFor = InvalidRefreshTokenException.class)
public class TokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final LoginCodeRepository loginCodeRepository;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthProperties properties;

    /** 새 기기 로그인: 새 family 로 토큰 한 쌍을 발급한다 */
    public IssuedTokens issue(User user) {
        return issue(user, UUID.randomUUID().toString());
    }

    /** 리프레시 토큰을 새 토큰 한 쌍으로 바꾼다 */
    public IssuedTokens refresh(String rawRefreshToken) {
        LocalDateTime now = LocalDateTime.now();
        RefreshToken token = refreshTokenRepository.findByTokenHash(TokenHasher.sha256(rawRefreshToken))
                .orElseThrow(() -> new InvalidRefreshTokenException("유효하지 않은 리프레시 토큰입니다."));
        if (token.getRevokedAt() != null || !token.getExpiresAt().isAfter(now)) {
            throw new InvalidRefreshTokenException("만료되었거나 폐기된 리프레시 토큰입니다. 다시 로그인해 주세요.");
        }
        if (refreshTokenRepository.markUsed(token.getId(), now) == 0) {
            refreshTokenRepository.revokeFamily(token.getFamilyId(), now);
            log.warn("[TokenService] 이미 쓴 리프레시 토큰 재사용: userId={}, familyId={}", token.getUserId(), token.getFamilyId());
            throw new InvalidRefreshTokenException("이미 사용된 리프레시 토큰입니다. 다시 로그인해 주세요.");
        }
        User user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new InvalidRefreshTokenException("사용자를 찾을 수 없습니다."));
        return issue(user, token.getFamilyId());
    }

    /** 로그아웃: 그 기기(family)의 리프레시 토큰을 폐기한다. 모르는 토큰이면 아무것도 하지 않는다 */
    public void logout(String rawRefreshToken) {
        refreshTokenRepository.findByTokenHash(TokenHasher.sha256(rawRefreshToken))
                .ifPresent(token -> refreshTokenRepository.revokeFamily(token.getFamilyId(), LocalDateTime.now()));
    }

    /** 사용자의 모든 기기를 로그아웃시킨다 (카카오 연결 끊기) */
    public int revokeAll(Long userId) {
        return refreshTokenRepository.revokeUser(userId, LocalDateTime.now());
    }

    /** 웹 OAuth 로그인 뒤 URL 로 넘길 1회용 코드를 만든다 */
    public String createLoginCode(Long userId) {
        String raw = TokenHasher.newRawToken();
        loginCodeRepository.save(LoginCode.builder()
                .userId(userId)
                .codeHash(TokenHasher.sha256(raw))
                .expiresAt(LocalDateTime.now().plus(properties.getToken().getLoginCodeTtl()))
                .build());
        return raw;
    }

    /** 1회용 코드를 토큰 한 쌍으로 바꾼다. 코드는 한 번만 쓸 수 있다 */
    public IssuedTokens exchangeLoginCode(String rawCode) {
        LocalDateTime now = LocalDateTime.now();
        LoginCode code = loginCodeRepository.findByCodeHash(TokenHasher.sha256(rawCode))
                .filter(found -> found.getExpiresAt().isAfter(now))
                .orElseThrow(() -> new InvalidRefreshTokenException("유효하지 않거나 만료된 로그인 코드입니다."));
        if (loginCodeRepository.markUsed(code.getId(), now) == 0) {
            throw new InvalidRefreshTokenException("이미 사용된 로그인 코드입니다.");
        }
        User user = userRepository.findById(code.getUserId())
                .orElseThrow(() -> new InvalidRefreshTokenException("사용자를 찾을 수 없습니다."));
        return issue(user);
    }

    private IssuedTokens issue(User user, String familyId) {
        String raw = TokenHasher.newRawToken();
        LocalDateTime expiresAt = LocalDateTime.now().plus(properties.getToken().getRefreshTtl());
        refreshTokenRepository.save(RefreshToken.builder()
                .userId(user.getId())
                .familyId(familyId)
                .tokenHash(TokenHasher.sha256(raw))
                .expiresAt(expiresAt)
                .build());
        return new IssuedTokens(jwtTokenProvider.generateAccessToken(user, properties.getToken().getAccessTtl()),
                properties.getToken().getAccessTtl().toSeconds(), raw, expiresAt);
    }
}
