package com.bosshi.maeul.ai;

/**
 * AI 생성 결과
 *
 * @param provider 결과를 만든 제공자 이름 (claude, gemini)
 */
public record AiResult<T>(String provider, T value) {
}
