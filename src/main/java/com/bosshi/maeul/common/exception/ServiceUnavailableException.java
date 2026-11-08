package com.bosshi.maeul.common.exception;

import org.springframework.modulith.NamedInterface;

/**
 * 외부 의존 서비스(AI 등)를 일시적으로 사용할 수 없을 때 발생. 503 으로 응답한다.
 */
@NamedInterface
public class ServiceUnavailableException extends RuntimeException {
    /** 사용 가능한 제공자나 키가 없을 때 */
    public static final String UNAVAILABLE_MESSAGE = "일정 생성 서비스를 일시적으로 사용할 수 없습니다. 잠시 후 다시 시도해주세요.";
    /** 제공자가 과부하이거나 요청이 제한되었을 때 */
    public static final String OVERLOADED_MESSAGE = "현재 AI 사용량이 많아 일정을 생성하지 못했습니다. 잠시 후 다시 시도해주세요.";

    public ServiceUnavailableException(String message) {
        super(message);
    }

    public ServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
