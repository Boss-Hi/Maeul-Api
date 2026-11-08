package com.bosshi.maeul.itineraries.itinerary.generator;

import com.bosshi.maeul.itineraries.ai.response.GeminiGenerateResponse;
import com.bosshi.maeul.itineraries.ai.service.GeminiService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Google Gemini 로 일정을 생성한다. (제공자 이름: gemini)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiItineraryGenerator extends AbstractAiItineraryGenerator {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final GeminiService geminiService;

    @Override
    public String providerName() {
        return "gemini";
    }

    @Override
    public boolean isAvailable() {
        return geminiService.hasAvailableApiKey();
    }

    @Override
    protected List<ItineraryDayJson> requestDays(String prompt) throws JsonProcessingException {
        GeminiGenerateResponse response = geminiService.generate(prompt);
        String result = response != null ? response.getFirstText() : null;
        log.info("Gemini API 호출 성공: {}", result);
        if (result == null || result.isBlank()) {
            throw new IllegalStateException("Gemini API 응답 결과가 비어있습니다.");
        }
        return OBJECT_MAPPER.readValue(cleanMarkdownJson(result), new TypeReference<>() {
        });
    }

    @Override
    protected String outputInstruction() {
        return """
                3. 출력은 설명, 인사말, 마크다운(```json) 없이 오직 아래 지정된 규격의 JSON 데이터만 반환하세요.
                [응답 JSON 스키마]
                [
                    {
                      "date": "2026-09-01",
                      "dayNumber": 1,
                      "items": [
                        {
                          "contentId": "관광지_contentId 또는 null",
                          "sequence": 1,
                          "title": "contentId 가 null 일 때 일정 제목",
                          "description": "contentId 가 null 일 때 일정 설명",
                          "startTime": "09:30",
                          "endTime": "11:00"
                        }
                      ]
                    }
                ]
                """;
    }

    static String cleanMarkdownJson(String raw) {
        if (raw == null) return null;
        raw = raw.trim();
        if (raw.startsWith("```json")) {
            raw = raw.substring(7);
        } else if (raw.startsWith("```")) {
            raw = raw.substring(3);
        }
        if (raw.endsWith("```")) {
            raw = raw.substring(0, raw.length() - 3);
        }
        return raw.trim();
    }
}
