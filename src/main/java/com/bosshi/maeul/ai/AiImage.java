package com.bosshi.maeul.ai;

/**
 * AI 요청에 함께 보내는 이미지.
 *
 * @param mimeType image/jpeg, image/png, image/webp 중 하나
 */
public record AiImage(byte[] data, String mimeType) {
}
