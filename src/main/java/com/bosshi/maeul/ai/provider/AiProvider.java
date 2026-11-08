package com.bosshi.maeul.ai.provider;

import com.bosshi.maeul.ai.AiRequest;

/**
 * AI 제공자 1개. 제공자 순서(ai.providers)의 이름과 {@link #name()} 이 같아야 한다.
 */
public interface AiProvider {

    String name();

    /**
     * 지금 호출 가능한지 (예: 활성 키 존재 여부)
     */
    boolean isAvailable();

    <T> T generate(AiRequest<T> request);
}
