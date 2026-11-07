package com.bosshi.maeul.common.exception;

import org.springframework.modulith.NamedInterface;

/**
 * 외부 의존 서비스(AI 등)를 일시적으로 사용할 수 없을 때 발생. 503 으로 응답한다.
 */
@NamedInterface
public class ServiceUnavailableException extends RuntimeException {
    public ServiceUnavailableException(String message) {
        super(message);
    }
}
