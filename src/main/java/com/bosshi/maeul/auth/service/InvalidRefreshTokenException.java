package com.bosshi.maeul.auth.service;

import org.springframework.security.core.AuthenticationException;

/** 없거나, 만료되었거나, 폐기되었거나, 이미 쓴 리프레시 토큰·로그인 코드. 401 로 응답한다 */
public class InvalidRefreshTokenException extends AuthenticationException {
    public InvalidRefreshTokenException(String message) {
        super(message);
    }
}
