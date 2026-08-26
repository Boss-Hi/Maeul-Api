package com.bosshi.maeul.ai.json;

public record GeminiJsonMetrics(
        int objectCount,
        int arrayCount,
        int leafValueCount,
        int maxDepth,
        int charLength,
        int byteLength
) {
}

