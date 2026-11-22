package com.bosshi.maeul.auth.oidc;

import org.springframework.security.core.AuthenticationException;

/** 서명, 발급자, 대상(aud), 만료, nonce 중 하나라도 맞지 않는 ID 토큰. 401 로 응답한다 */
public class InvalidIdTokenException extends AuthenticationException {
    public InvalidIdTokenException(String message) {
        super(message);
    }

    public InvalidIdTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
