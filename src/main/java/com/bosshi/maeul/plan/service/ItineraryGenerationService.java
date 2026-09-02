package com.bosshi.maeul.plan.service;

import com.bosshi.maeul.ai.request.GeminiGenerateRequest;
import com.bosshi.maeul.ai.response.GeminiGenerateResponse;
import com.bosshi.maeul.ai.service.GeminiService;
import com.bosshi.maeul.openapi.entity.Festival;
import com.bosshi.maeul.plan.dto.PlanGenerateDTO;
import com.bosshi.maeul.plan.entity.Itinerary;
import com.bosshi.maeul.plan.entity.ItineraryDay;
import com.bosshi.maeul.plan.entity.Trip;
import com.bosshi.maeul.plan.repository.ItineraryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * 일정 생성 서비스
 * <p>
 * Google Gemini API를 사용하여 필터링된 관광지 정보를 기반으로
 * AI가 추천하는 여행 일정을 생성합니다.
 * <p>
 * Phase 1: 프로토타입 (Mock 데이터 반환)
 * Phase 2: 실제 Gemini API 통합
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
    public Itinerary generateItinerary(PlanGenerateDTO dto) {
        // Gemini API 프롬프트 구성
        String prompt = buildItineraryPrompt(dto);

        // Gemini API 호출
        GeminiGenerateResponse geminiResponse = geminiService.generate(GeminiGenerateRequest.ofPrompt(prompt));

        // 응답을 Itinerary 객체로 파싱
        Itinerary itinerary = parseItineraryFromResponse(geminiResponse != null ? geminiResponse.getFirstText() : null, dto);

        // 4. DB에 저장
        Itinerary savedItinerary = itineraryRepository.save(itinerary);

        return savedItinerary;
    }

    /**
     * Gemini API를 위한 프롬프트를 구성합니다.
     */
    private String buildItineraryPrompt(PlanGenerateDTO dto) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("당신은 한국 여행 일정 추천 전문가입니다.\n");
        prompt.append("다음 정보를 바탕으로 여행객을 위한 일정을 짜주세요.\n\n");

        // 여행 기본 정보
        prompt.append("[여행 기본 정보]\n");
        prompt.append("목적지: ").append(dto.getFestival().getAddr1()).append("\n");
        prompt.append("시작일: ").append(dto.getStartDate()).append("\n");
        prompt.append("종료일: ").append(dto.getEndDate()).append("\n");
        prompt.append("여행 기간: ")
                .append(calculateDaysBetween(dto.getStartDate(), dto.getEndDate()) + 1)
                .append("박\n");
        prompt.append("선택 카테고리: ").append(String.join(", ", dto.getSelectedCategories())).append("\n\n");

        // 주축제 정보
        prompt.append("[주축제 정보]\n");
        prompt.append("축제명: ").append(dto.getFestival().getTitle()).append("\n");
        prompt.append("개최 기간: ")
                .append(dto.getFestival().getEventStartDate())
                .append(" ~ ")
                .append(dto.getFestival().getEventEndDate())
                .append("\n");
        prompt.append("위치: ")
                .append(dto.getFestival().getAddr1())
                .append(" ")
                .append(dto.getFestival().getAddr2())
                .append("\n\n");

        // 추천 관광지 목록
        prompt.append("[추천 가능한 관광지]\n");
        List<Festival> filteredVenues = dto.getRecommendableFestivals();
        if (filteredVenues != null) {
            for (int i = 0; i < filteredVenues.size() && i < 20; i++) {
                Festival venue = filteredVenues.get(i);
                prompt.append(i + 1)
                        .append(". ")
                        .append(venue.getTitle())
                        .append(" (")
                        .append(venue.getAddr1())
                        .append(")\n");
            }
        }

        prompt.append("\n");
        prompt.append("[지시사항]\n");
        prompt.append("1. 주축제를 중심으로 일정을 짜세요.\n");
        prompt.append("2. 각 날짜별로 3-5개의 관광지를 추천하세요.\n");
        prompt.append("3. 선택한 카테고리의 관광지만 포함하세요.\n");
        prompt.append("4. 응답은 반드시 다음 JSON 형식으로 해주세요:\n");
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
        prompt.append("          \"sequence\": 1\n");
        prompt.append("        }\n");
        prompt.append("      ]\n");
        prompt.append("    }\n");
        prompt.append("  ]\n");
        prompt.append("}\n");

        return prompt.toString();
    }

    private int calculateDaysBetween(String startDate, String endDate) {
        // 날짜 문자열을 LocalDate로 변환
        LocalDate start = LocalDate.parse(startDate, DateTimeFormatter.BASIC_ISO_DATE);
        LocalDate end = LocalDate.parse(endDate, DateTimeFormatter.BASIC_ISO_DATE);
        // 두 날짜 사이의 일수 계산
        return (int) ChronoUnit.DAYS.between(start, end);
    }

    /**
     * Gemini 응답을 Itinerary 객체로 파싱합니다.
     */
    private Itinerary parseItineraryFromResponse(String response, PlanGenerateDTO dto) {
        log.info("응답 파싱 시작");

        Trip trip = Trip.builder()
                .destination("")
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .build();

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
            LocalDate currentDate = LocalDate.parse(dto.getStartDate());
            int dayNumber = 1;

            while (!currentDate.isAfter(LocalDate.parse(dto.getEndDate()))) {
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
}
