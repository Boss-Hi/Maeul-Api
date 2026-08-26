package com.bosshi.maeul.plan.service;

import com.bosshi.maeul.ai.config.GeminiConfig;
import com.bosshi.maeul.openapi.domain.Venue;
import com.bosshi.maeul.plan.domain.*;
import com.bosshi.maeul.plan.repository.ItineraryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * 일정 생성 서비스
 *
 * Google Gemini API를 사용하여 필터링된 관광지 정보를 기반으로
 * AI가 추천하는 여행 일정을 생성합니다.
 *
 * Phase 1: 프로토타입 (Mock 데이터 반환)
 * Phase 2: 실제 Gemini API 통합
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ItineraryGenerationService {

    private final GeminiConfig geminiConfig;
    private final ItineraryRepository itineraryRepository;
    private final RestClient restClient = RestClient.create();

    /**
     * Trip 정보와 필터링된 관광지 목록을 기반으로 일정을 생성합니다.
     *
     * @param trip 여행 정보
     * @param mainFestival 주축제 정보
     * @param filteredVenues 필터링된 관광지 목록
     * @return 생성된 Itinerary 객체
     */
    public Itinerary generateItinerary(Trip trip, MainFestival mainFestival, List<Venue> filteredVenues) {
        log.info("일정 생성 시작: Trip ID={}", trip.getId());

        try {
            // 1. Gemini API 프롬프트 구성
            String prompt = buildItineraryPrompt(trip, mainFestival, filteredVenues);
            log.info("프롬프트 길이: {} 글자", prompt.length());

            // 2. Gemini API 호출
            String geminiResponse = callGeminiApi(prompt);
            log.info("Gemini 응답 길이: {} 글자", geminiResponse.length());

            // 3. 응답을 Itinerary 객체로 파싱
            Itinerary itinerary = parseItineraryFromResponse(geminiResponse, trip);

            // 4. DB에 저장
            Itinerary savedItinerary = itineraryRepository.save(itinerary);
            log.info("일정 저장 완료: ID={}", savedItinerary.getId());

            return savedItinerary;
        } catch (Exception e) {
            log.error("일정 생성 중 오류 발생", e);
            throw new RuntimeException("일정 생성 실패: " + e.getMessage());
        }
    }

    /**
     * Gemini API를 위한 프롬프트를 구성합니다.
     */
    private String buildItineraryPrompt(Trip trip, MainFestival mainFestival, List<Venue> filteredVenues) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("당신은 한국 여행 일정 추천 전문가입니다.\n");
        prompt.append("다음 정보를 바탕으로 여행객을 위한 일정을 짜주세요.\n\n");

        // 여행 기본 정보
        prompt.append("[여행 기본 정보]\n");
        prompt.append("목적지: ").append(trip.getDestination()).append("\n");
        prompt.append("시작일: ").append(trip.getStartDate()).append("\n");
        prompt.append("종료일: ").append(trip.getEndDate()).append("\n");
        prompt.append("여행 기간: ").append(ChronoUnit.DAYS.between(trip.getStartDate(), trip.getEndDate()) + 1).append("박\n");
        prompt.append("선택 카테고리: ").append(String.join(", ", trip.getSelectedCategoriesList())).append("\n\n");

        // 주축제 정보
        prompt.append("[주축제 정보]\n");
        prompt.append("축제명: ").append(mainFestival.getTitle()).append("\n");
        prompt.append("개최 기간: ").append(mainFestival.getStartDate()).append(" ~ ").append(mainFestival.getEndDate()).append("\n");
        prompt.append("위치: ").append(mainFestival.getAddress1()).append(" ").append(mainFestival.getAddress2()).append("\n\n");

        // 추천 관광지 목록
        prompt.append("[추천 가능한 관광지]\n");
        for (int i = 0; i < filteredVenues.size() && i < 20; i++) {
            Venue venue = filteredVenues.get(i);
            prompt.append(i + 1).append(". ").append(venue.getTitle()).append(" (").append(venue.getCategory()).append(")\n");
            if (venue.getOverview() != null) {
                prompt.append("   설명: ").append(venue.getOverview().substring(0, Math.min(100, venue.getOverview().length()))).append("\n");
            }
        }

        prompt.append("\n");
        prompt.append("[지시사항]\n");
        prompt.append("1. 주축제를 중심으로 일정을 짜세요.\n");
        prompt.append("2. 각 날짜별로 3-5개의 관광지를 추천하세요.\n");
        prompt.append("3. 관광지마다 방문 시간대(예: 09:00-12:00)를 지정하세요.\n");
        prompt.append("4. 선택한 카테고리의 관광지만 포함하세요.\n");
        prompt.append("5. 응답은 반드시 다음 JSON 형식으로 해주세요:\n");
        prompt.append("{\n");
        prompt.append("  \"summary\": \"여행 전체 요약\",\n");
        prompt.append("  \"days\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"date\": \"2026-09-01\",\n");
        prompt.append("      \"dayNumber\": 1,\n");
        prompt.append("      \"theme\": \"테마명\",\n");
        prompt.append("      \"venues\": [\n");
        prompt.append("        {\n");
        prompt.append("          \"name\": \"장소명\",\n");
        prompt.append("          \"category\": \"카테고리\",\n");
        prompt.append("          \"visitTime\": \"09:00-12:00\",\n");
        prompt.append("          \"duration\": \"3시간\",\n");
        prompt.append("          \"sequence\": 1\n");
        prompt.append("        }\n");
        prompt.append("      ]\n");
        prompt.append("    }\n");
        prompt.append("  ]\n");
        prompt.append("}\n");

        return prompt.toString();
    }

    /**
     * Gemini API를 호출합니다.
     */
    private String callGeminiApi(String prompt) {
        log.info("Gemini API 호출 시작");

        if (!geminiConfig.isConfigured()) {
            log.warn("Gemini API가 설정되지 않음. Mock 응답 반환");
            return getMockItineraryResponse();
        }

        try {
            // TODO: Phase 2에서 실제 API 호출 구현
            // RestClient를 사용하여 Gemini API에 POST 요청
            // String url = geminiConfig.getBaseUrl() + "/v1beta/models/" + geminiConfig.getModel() + ":generateContent";
            // Map<String, Object> request = new HashMap<>();
            // request.put("contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))));
            //
            // String response = restClient.post()
            //     .uri(url + "?key=" + geminiConfig.getApiKey())
            //     .body(request)
            //     .retrieve()
            //     .body(String.class);

            log.warn("프로토타입: Gemini API 호출 미구현, Mock 응답 반환");
            return getMockItineraryResponse();
        } catch (Exception e) {
            log.error("Gemini API 호출 실패, Mock 응답 반환", e);
            return getMockItineraryResponse();
        }
    }

    /**
     * Gemini 응답을 Itinerary 객체로 파싱합니다.
     */
    private Itinerary parseItineraryFromResponse(String response, Trip trip) {
        log.info("응답 파싱 시작");

        Itinerary itinerary = Itinerary.builder()
                .trip(trip)
                .summary("프로토타입 일정: 실제 구현 필요")
                .days(new ArrayList<>())
                .build();

        try {
            // TODO: JSON 파싱 구현
            // Gson, Jackson, JSON-B 등을 사용하여 응답 파싱
            // 현재는 프로토타입이므로 스켈레톤만 작성

            // 임시: 기본 구조만 생성
            LocalDate currentDate = trip.getStartDate();
            int dayNumber = 1;

            while (!currentDate.isAfter(trip.getEndDate())) {
                ItineraryDay day = ItineraryDay.builder()
                        .itinerary(itinerary)
                        .date(currentDate)
                        .dayNumber(dayNumber)
                        .theme("프로토타입 테마")
                        .venues(new ArrayList<>())
                        .build();

                itinerary.getDays().add(day);

                currentDate = currentDate.plusDays(1);
                dayNumber++;
            }

            log.info("응답 파싱 완료: {} 일정 생성", itinerary.getDays().size());
            return itinerary;
        } catch (Exception e) {
            log.error("응답 파싱 중 오류", e);
            throw new RuntimeException("응답 파싱 실패: " + e.getMessage());
        }
    }

    /**
     * Mock 일정 응답을 반환합니다. (프로토타입용)
     */
    private String getMockItineraryResponse() {
        return "{\n" +
                "  \"summary\": \"프로토타입 여행 일정입니다.\",\n" +
                "  \"days\": [\n" +
                "    {\n" +
                "      \"date\": \"2026-09-01\",\n" +
                "      \"dayNumber\": 1,\n" +
                "      \"theme\": \"도시 탐방\",\n" +
                "      \"venues\": [\n" +
                "        {\n" +
                "          \"name\": \"축제 중심지\",\n" +
                "          \"category\": \"축제/공연\",\n" +
                "          \"visitTime\": \"14:00-18:00\",\n" +
                "          \"duration\": \"4시간\",\n" +
                "          \"sequence\": 1\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";
    }
}



