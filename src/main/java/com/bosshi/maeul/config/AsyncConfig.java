package com.bosshi.maeul.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 비동기 처리 설정 클래스
 *
 * @Async 어노테이션을 사용하여 비동기로 메서드를 실행할 수 있습니다.
 */
@Configuration
@EnableAsync
public class AsyncConfig {
    // Spring의 기본 AsyncExecutor를 사용합니다.
    // 필요하면 커스텀 Executor를 정의할 수 있습니다.
}

