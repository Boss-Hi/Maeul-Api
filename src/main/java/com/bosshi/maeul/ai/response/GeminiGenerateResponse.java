package com.bosshi.maeul.ai.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeminiGenerateResponse {
    private List<Candidate> candidates = new ArrayList<>();
    private UsageMetadata usageMetadata;
    private String modelVersion;

    public String getFirstText() {
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }

        Candidate candidate = candidates.get(0);
        if (candidate == null || candidate.getContent() == null || candidate.getContent().getParts() == null
                || candidate.getContent().getParts().isEmpty()) {
            return null;
        }

        return candidate.getContent().getParts().get(0).getText();
    }

    /** 첫 후보의 종료 사유 (STOP, MAX_TOKENS, SAFETY 등). 후보가 없으면 null */
    public String getFirstFinishReason() {
        if (candidates == null || candidates.isEmpty() || candidates.get(0) == null) {
            return null;
        }
        return candidates.get(0).getFinishReason();
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Candidate {
        private Content content;
        private String finishReason;
        private Double avgLogprobs;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Content {
        private String role;
        private List<Part> parts = new ArrayList<>();
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Part {
        private String text;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UsageMetadata {
        private Integer promptTokenCount;
        private Integer candidatesTokenCount;
        private Integer totalTokenCount;
    }
}

