package com.bosshi.maeul.ai.batch;

import com.bosshi.maeul.ai.config.GeminiConfig;
import com.bosshi.maeul.ai.json.GeminiJsonMetrics;
import com.bosshi.maeul.ai.json.GeminiJsonQuantifier;
import com.bosshi.maeul.ai.response.GeminiGenerateResponse;
import com.bosshi.maeul.ai.service.GeminiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class GeminiHealthBatch {
    private final GeminiService geminiService;
    private final GeminiConfig config;
    private final GeminiJsonQuantifier geminiJsonQuantifier;

    @Scheduled(cron = "${ai.gemini.batch-cron:0 0/30 * * * *}")
    public void runHealthCheck() {
        if (!config.isBatchEnabled()) {
            return;
        }

        if (!StringUtils.hasText(config.getBatchPrompt())) {
            log.warn("Gemini batch skipped: batchPrompt is blank.");
            return;
        }

        GeminiGenerateResponse response = geminiService.generateText(config.getBatchPrompt());
        GeminiJsonMetrics metrics = geminiJsonQuantifier.quantify(response);

        log.info(
                "Gemini batch success - text='{}', objects={}, arrays={}, leafValues={}, depth={}, bytes={}",
                abbreviate(response.getFirstText(), 80),
                metrics.objectCount(),
                metrics.arrayCount(),
                metrics.leafValueCount(),
                metrics.maxDepth(),
                metrics.byteLength()
        );
    }

    private String abbreviate(String value, int maxLength) {
        if (!StringUtils.hasText(value)) {
            return "(empty)";
        }

        if (value.length() <= maxLength) {
            return value;
        }

        return value.substring(0, maxLength) + "...";
    }
}

