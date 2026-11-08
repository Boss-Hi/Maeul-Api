package com.bosshi.maeul.ai.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class GeminiJsonQuantifier {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GeminiJsonMetrics quantify(Object payload) {
        try {
            return quantifyJson(objectMapper.writeValueAsString(payload));
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to serialize payload for quantification.", e);
        }
    }

    public GeminiJsonMetrics quantifyJson(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            Counter counter = new Counter();
            count(root, 0, counter);

            return new GeminiJsonMetrics(
                    counter.objectCount,
                    counter.arrayCount,
                    counter.leafValueCount,
                    counter.maxDepth,
                    json.length(),
                    json.getBytes(StandardCharsets.UTF_8).length
            );
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to parse json for quantification.", e);
        }
    }

    private void count(JsonNode node, int depth, Counter counter) {
        counter.maxDepth = Math.max(counter.maxDepth, depth);

        if (node.isObject()) {
            counter.objectCount++;
            node.properties().forEach(entry -> count(entry.getValue(), depth + 1, counter));
            return;
        }

        if (node.isArray()) {
            counter.arrayCount++;
            node.forEach(child -> count(child, depth + 1, counter));
            return;
        }

        counter.leafValueCount++;
    }

    private static class Counter {
        private int objectCount;
        private int arrayCount;
        private int leafValueCount;
        private int maxDepth;
    }
}
