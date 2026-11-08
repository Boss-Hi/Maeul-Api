package com.bosshi.maeul.ai.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AiProviderType {
    GEMINI("Google Gemini"),
    GPT("OpenAI GPT"),
    CLAUDE("Anthropic Claude");

    private final String description;
}