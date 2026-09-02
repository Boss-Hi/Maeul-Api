package com.bosshi.maeul.itinerary.service;

import com.bosshi.maeul.ai.service.GeminiService;
import com.bosshi.maeul.itinerary.dto.ItineraryGenerateDTO;
import com.bosshi.maeul.itinerary.entity.Itinerary;
import com.bosshi.maeul.itinerary.entity.ItineraryDay;
import com.bosshi.maeul.itinerary.entity.ItineraryTour;
import com.bosshi.maeul.itinerary.repository.ItineraryRepository;
import com.bosshi.maeul.openapi.entity.Tour;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * 일정 생성 서비스
 * <p>
 * Google Gemini API를 사용하여 필터링된 관광지 정보를 기반으로
 * AI가 추천하는 여행 일정을 생성합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ItineraryGenerationService {

    private final ItineraryRepository itineraryRepository;
    private final GeminiService geminiService;

    /**
     * Trip 정보와 필터링된 관광지 목록을 기반으로 일정을 생성합니다.
     *
     * @return 생성된 Itinerary 객체
     */
    public Itinerary generateItinerary(ItineraryGenerateDTO dto) {
        // Gemini API 프롬프트 구성
        String prompt = buildItineraryPrompt(dto);

        // Gemini API 호출 (실패 시 예외 처리 및 Mock 데이터로 Fallback)
        String result;
            /*GeminiGenerateResponse geminiResponse = geminiService.generate(prompt);
            result = geminiResponse != null ? geminiResponse.getFirstText() : null;
            log.info("Gemini API 호출 성공: {}", result);
            if (result == null || result.isBlank()) {
                throw new IllegalStateException("Gemini API 응답 결과가 비어있습니다.");
            }*/
        result = "[ {\n" +
                "    \"date\" : \"2026-09-01\",\n" +
                "    \"dayNumber\" : 1,\n" +
                "    \"items\" : [ {\n" +
                "      \"contentId\" : \"130699\",\n" +
                "      \"sequence\" : 1\n" +
                "    }, {\n" +
                "      \"contentId\" : \"142728\",\n" +
                "      \"sequence\" : 2\n" +
                "    }, {\n" +
                "      \"contentId\" : \"1255726\",\n" +
                "      \"sequence\" : 3\n" +
                "    } ]\n" +
                "  }, {\n" +
                "    \"date\" : \"2026-09-02\",\n" +
                "    \"dayNumber\" : 2,\n" +
                "    \"items\" : [ {\n" +
                "      \"contentId\" : \"130231\",\n" +
                "      \"sequence\" : 1\n" +
                "    }, {\n" +
                "      \"contentId\" : \"130353\",\n" +
                "      \"sequence\" : 2\n" +
                "    }, {\n" +
                "      \"contentId\" : \"130346\",\n" +
                "      \"sequence\" : 3\n" +
                "    }, {\n" +
                "      \"contentId\" : \"129724\",\n" +
                "      \"sequence\" : 4\n" +
                "    } ]\n" +
                "  }, {\n" +
                "    \"date\" : \"2026-09-03\",\n" +
                "    \"dayNumber\" : 3,\n" +
                "    \"items\" : [ {\n" +
                "      \"contentId\" : \"142733\",\n" +
                "      \"sequence\" : 1\n" +
                "    }, {\n" +
                "      \"contentId\" : \"1019327\",\n" +
                "      \"sequence\" : 2\n" +
                "    }, {\n" +
                "      \"contentId\" : \"1105871\",\n" +
                "      \"sequence\" : 3\n" +
                "    } ]\n" +
                "  } ]";

        return parseItineraryFromResponse(result, dto);
    }

    /**
     * Gemini API를 위한 프롬프트를 구성합니다.
     */
    private String buildItineraryPrompt(ItineraryGenerateDTO dto) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("당신은 한국 여행 일정 추천 전문가입니다.\n");
        prompt.append("다음 정보를 바탕으로 여행객을 위한 일정을 짜주세요.\n\n");

        // 여행 기본 정보
        prompt.append("[여행 기본 정보]\n");
        if (dto.getTour() != null) {
            prompt.append("목적지: ").append(dto.getTour().getAddr1()).append("\n");
        }
        prompt.append("시작일: ").append(dto.getStartDate()).append("\n");
        prompt.append("종료일: ").append(dto.getEndDate()).append("\n");
        prompt.append("여행 기간: ")
                .append(calculateDaysBetween(dto.getStartDate(), dto.getEndDate()) + 1)
                .append("일\n");

        // 주축제 정보
        if (dto.getTour() != null) {
            prompt.append("[주축제 정보]\n");
            prompt.append("축제명: ").append(dto.getTour().getTitle()).append("\n");
            prompt.append("위치: ")
                    .append(dto.getTour().getAddr1())
                    .append(" ")
                    .append(dto.getTour().getAddr2())
                    .append("\n\n");
        }

        // 추천 관광지 목록
        prompt.append("[추천 가능한 관광지]\n");
        List<Tour> filteredVenues = dto.getRecommendableTours();
        if (filteredVenues != null) {
            for (int i = 0; i < filteredVenues.size() && i < 20; i++) {
                Tour venue = filteredVenues.get(i);
                prompt.append("- ")
                        .append(venue.getTitle())
                        .append(" (ID: ").append(venue.getContentId()).append(")");
                if (venue.getAddr1() != null) {
                    prompt.append(", 주소: ").append(venue.getAddr1());
                }
                prompt.append("\n");
            }
        }

        prompt.append("\n");
        prompt.append("[지시사항]\n");
        prompt.append("1. 주축제를 중심으로 일정을 짜세요.\n");
        prompt.append("2. 각 날짜별로 3-5개의 관광지를 추천하세요.\n");
        prompt.append("3. 출력은 설명, 인사말, 마크다운(```json) 없이 오직 아래 지정된 규격의 JSON 데이터만 반환하세요.\n");
        prompt.append("[응답 JSON 스키마]\n");
        prompt.append("[\n");
        prompt.append("    {\n");
        prompt.append("      \"date\": \"2026-09-01\",\n");
        prompt.append("      \"dayNumber\": 1,\n");
        prompt.append("      \"items\": [\n");
        prompt.append("        {\n");
        prompt.append("          \"contentId\": \"관광지_contentId\",\n");
        prompt.append("          \"sequence\": 1\n");
        prompt.append("        }\n");
        prompt.append("      ]\n");
        prompt.append("    }\n");
        prompt.append("]\n");

        return prompt.toString();
    }

    private int calculateDaysBetween(LocalDate startDate, LocalDate endDate) {
        return (int) ChronoUnit.DAYS.between(startDate, endDate);
    }

    /**
     * Gemini 응답을 Itinerary 객체로 파싱합니다.
     */
    private Itinerary parseItineraryFromResponse(String json, ItineraryGenerateDTO dto) {
        log.info("응답 파싱 시작");
        String cleanedJson = cleanMarkdownJson(json);
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            List<ItineraryDayJson> daysJson = objectMapper.readValue(
                    cleanedJson, new TypeReference<List<ItineraryDayJson>>() {
                    }
            );

            Itinerary itinerary = Itinerary.builder()
                    .startDate(dto.getStartDate())
                    .endDate(dto.getEndDate())
                    .tour(dto.getTour())
                    .build();

            List<ItineraryDay> itineraryDays = new ArrayList<>();
            for (ItineraryDayJson dayJson : daysJson) {
                ItineraryDay day = ItineraryDay.builder()
                        .itinerary(itinerary)
                        .date(LocalDate.parse(dayJson.date()))
                        .dayNumber(dayJson.dayNumber())
                        .venues(new ArrayList<>())
                        .build();

                if (dayJson.items() != null) {
                    for (ItineraryTourJson tourJson : dayJson.items()) {
                        ItineraryTour tour = ItineraryTour.builder()
                                .itineraryDay(day)
                                .contentId(tourJson.contentId())
                                .sequence(tourJson.sequence())
                                .build();
                        day.getVenues().add(tour);
                    }
                }
                itineraryDays.add(day);
            }

            itinerary.setItineraryDays(itineraryDays);

            Itinerary savedItinerary = itineraryRepository.save(itinerary);
            log.info(
                    "응답 파싱 및 DB 저장 완료: ID={}, 일수={}", savedItinerary.getId(), savedItinerary.getItineraryDays()
                            .size()
            );
            return savedItinerary;

        } catch (Exception e) {
            log.error("응답 파싱 및 DB 저장 중 오류", e);
            throw new RuntimeException("응답 파싱 실패: " + e.getMessage(), e);
        }
    }

    private String cleanMarkdownJson(String raw) {
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

    private record ItineraryDayJson(
            String date,
            Integer dayNumber,
            List<ItineraryTourJson> items
    ) {
    }

    private record ItineraryTourJson(
            String contentId,
            Integer sequence
    ) {
    }
}
