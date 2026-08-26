package com.bosshi.maeul.ai.json;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GeminiJsonQuantifierTest {

    private final GeminiJsonQuantifier quantifier = new GeminiJsonQuantifier();

    @Test
    void quantifyJsonCountsStructureAndDepth() {
        String json = """
                {
                  "a": 1,
                  "b": [
                    {
                      "c": "x"
                    },
                    true
                  ],
                  "d": {
                    "e": null
                  }
                }
                """;

        GeminiJsonMetrics metrics = quantifier.quantifyJson(json);

        assertThat(metrics.objectCount()).isEqualTo(3);
        assertThat(metrics.arrayCount()).isEqualTo(1);
        assertThat(metrics.leafValueCount()).isEqualTo(4);
        assertThat(metrics.maxDepth()).isEqualTo(3);
        assertThat(metrics.charLength()).isGreaterThan(0);
        assertThat(metrics.byteLength()).isGreaterThan(0);
    }
}
