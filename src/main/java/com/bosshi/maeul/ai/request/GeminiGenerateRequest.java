package com.bosshi.maeul.ai.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.modulith.NamedInterface;

import java.util.List;

@NamedInterface
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GeminiGenerateRequest {
    private static final double DEFAULT_TEMPERATURE = 0.7;
    private static final int DEFAULT_MAX_OUTPUT_TOKENS = 1024;

    private List<Content> contents;
    private GenerationConfig generationConfig;

    public static GeminiGenerateRequest ofPrompt(String prompt) {
        return ofPrompt(prompt, DEFAULT_TEMPERATURE, DEFAULT_MAX_OUTPUT_TOKENS);
    }

    public static GeminiGenerateRequest ofPrompt(String prompt, Double temperature) {
        return ofPrompt(prompt, temperature, DEFAULT_MAX_OUTPUT_TOKENS);
    }

    public static GeminiGenerateRequest ofPrompt(String prompt, Double temperature, Integer maxOutputTokens) {
        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException("Can not create GeminiGenerateRequest with null or blank prompt");
        }
        
        Part part = new Part(prompt);
        Content content = new Content("user", List.of(part));
        GenerationConfig config = new GenerationConfig(temperature, maxOutputTokens, null);
        return new GeminiGenerateRequest(List.of(content), config);
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Content {
        private String role;
        private List<Part> parts;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Part {
        private String text;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class GenerationConfig {
        private Double temperature;
        private Integer maxOutputTokens;
        private String responseMimeType;
    }
}

