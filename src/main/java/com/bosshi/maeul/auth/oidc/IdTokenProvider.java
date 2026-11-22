package com.bosshi.maeul.auth.oidc;

import com.bosshi.maeul.auth.type.AuthType;

import java.util.Locale;

/** 앱이 네이티브 SDK 로 로그인해 ID 토큰을 받는 로그인 수단 */
public enum IdTokenProvider {
    KAKAO(AuthType.KAKAO),
    APPLE(AuthType.APPLE);

    private final AuthType authType;

    IdTokenProvider(AuthType authType) {
        this.authType = authType;
    }

    public AuthType authType() {
        return authType;
    }

    public static IdTokenProvider from(String path) {
        try {
            return valueOf(path.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("지원하지 않는 로그인 수단입니다: " + path);
        }
    }
}
