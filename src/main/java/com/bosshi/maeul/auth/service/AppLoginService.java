package com.bosshi.maeul.auth.service;

import com.bosshi.maeul.auth.oidc.IdTokenProvider;
import com.bosshi.maeul.auth.oidc.IdTokenVerifier;
import com.bosshi.maeul.auth.oidc.VerifiedIdentity;
import com.bosshi.maeul.auth.type.AuthType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 앱 로그인과 카카오 연결 끊기 (ADR 0015)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AppLoginService {

    private final IdTokenVerifier idTokenVerifier;
    private final AccountService accountService;
    private final TokenService tokenService;

    /** ID 토큰을 검증하고 계정을 찾거나 만든 뒤 새 기기 로그인으로 토큰 한 쌍을 발급한다 */
    @Transactional
    public IssuedTokens login(IdTokenProvider provider, String idToken, String nonce, String nickname) {
        VerifiedIdentity identity = idTokenVerifier.verify(provider, idToken, nonce);
        String name = identity.nickname() != null ? identity.nickname() : nickname;
        return tokenService.issue(accountService.findOrCreate(provider.authType(), identity.subject(), name));
    }

    /** 카카오 연결 끊기: 그 사용자의 모든 기기를 로그아웃시킨다. 계정과 기록은 남긴다 */
    @Transactional
    public void kakaoUnlinked(String kakaoUserId) {
        accountService.find(AuthType.KAKAO, kakaoUserId).ifPresentOrElse(
                user -> log.info("[AppLoginService] 카카오 연결 끊기: userId={}, 폐기한 리프레시 토큰 {}개", user.getId(), tokenService.revokeAll(user.getId())),
                () -> log.info("[AppLoginService] 카카오 연결 끊기: 모르는 카카오 사용자 {}", kakaoUserId));
    }
}
