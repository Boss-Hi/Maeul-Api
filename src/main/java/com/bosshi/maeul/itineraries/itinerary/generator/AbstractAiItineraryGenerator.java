package com.bosshi.maeul.itineraries.itinerary.generator;

import com.bosshi.maeul.itineraries.itinerary.dto.ItineraryGenerateDTO;
import com.bosshi.maeul.itineraries.itinerary.dto.SurveyAnswerInfo;
import com.bosshi.maeul.itineraries.itinerary.entity.Itinerary;
import com.bosshi.maeul.itineraries.itinerary.entity.ItineraryDay;
import com.bosshi.maeul.itineraries.itinerary.generator.impl.ItineraryGeneratorImpl;
import com.bosshi.maeul.itineraries.itinerary.type.Pace;
import com.bosshi.maeul.itineraries.openapi.entity.Tour;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AI 제공자 공통 일정 생성 흐름: 프롬프트 구성 → 제공자 호출 → 일정 엔티티 조립.
 * 제공자별 구현은 {@link #requestDays} 와 {@link #outputInstruction} 만 담당한다.
 */
public abstract class AbstractAiItineraryGenerator implements ItineraryGeneratorImpl {

    @Override
    public Itinerary generateItinerary(ItineraryGenerateDTO dto) throws JsonProcessingException {
        if (dto.getRecommendableTours() == null || dto.getRecommendableTours().isEmpty()) {
            throw new IllegalStateException("추천 가능한 관광지 목록이 비어있습니다.");
        }

        List<ItineraryDayJson> days = requestDays(buildItineraryPrompt(dto));
        if (days == null || days.isEmpty()) {
            throw new IllegalStateException(providerName() + " 응답에 일정이 없습니다.");
        }
        return buildItinerary(days, dto);
    }

    /**
     * 제공자를 호출해 일별 일정을 받는다.
     */
    protected abstract List<ItineraryDayJson> requestDays(String prompt) throws JsonProcessingException;

    /**
     * 응답 형식 지시문 (제공자마다 JSON 강제 방식이 다름)
     */
    protected abstract String outputInstruction();

    String buildItineraryPrompt(ItineraryGenerateDTO dto) {
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
                .append(ChronoUnit.DAYS.between(dto.getStartDate(), dto.getEndDate()) + 1)
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

        // 사용자 설문 답변
        if (dto.getSurveyAnswers() != null && !dto.getSurveyAnswers().isEmpty()) {
            prompt.append("\n[여행객 정보 (사용자가 설문에 직접 입력한 값)]\n");
            surveyAnswerLines(dto.getSurveyAnswers()).forEach(line -> prompt.append(line).append("\n"));
            prompt.append("※ 일정 템포를 답했더라도, 아래 [일정 설정]의 페이스를 우선하세요.\n");
        }

        // 일정 설정
        Pace pace = dto.getPace() != null ? dto.getPace() : Pace.MODERATE;
        LocalTime dayStart = dto.getDayStartTime() != null ? dto.getDayStartTime() : ItineraryGenerateDTO.DEFAULT_DAY_START;
        LocalTime dayEnd = dto.getDayEndTime() != null ? dto.getDayEndTime() : ItineraryGenerateDTO.DEFAULT_DAY_END;
        prompt.append("\n[일정 설정]\n");
        prompt.append("페이스: ").append(pace.name()).append(" (").append(pace.venueRange()).append(")\n");
        prompt.append("하루 시간대: ").append(dayStart).append(" ~ ").append(dayEnd).append("\n");

        // 추천 관광지 목록
        prompt.append("\n[추천 가능한 관광지]\n");
        List<Tour> filteredVenues = dto.getRecommendableTours();
        for (int i = 0; i < filteredVenues.size() && i < 20; i++) {
            Tour tour = filteredVenues.get(i);
            prompt.append("- ")
                    .append(tour.getTitle())
                    .append(" (contentId: ").append(tour.getContentId()).append(")");
            if (tour.getAddr1() != null) {
                prompt.append(", 주소: ").append(tour.getAddr1());
            }
            prompt.append("\n");
        }

        prompt.append("\n");
        prompt.append("[지시사항]\n");
        prompt.append("1. 주축제를 중심으로 일정을 짜세요.\n");
        prompt.append("2. 각 날짜별로 ").append(pace.venueRange()).append("의 일정을 추천하세요.\n");
        prompt.append("   - 모든 일정의 방문 시간(startTime, endTime)은 ").append(dayStart).append(" ~ ").append(dayEnd)
                .append(" 안에서 HH:mm 형식으로 정하고, 같은 날 일정끼리 시간이 겹치지 않게 하세요.\n");
        prompt.append("   - 위 추천 관광지 목록의 장소를 쓸 때는 해당 contentId 를 그대로 넣으세요.\n");
        prompt.append("   - 목록에 없는 장소나 활동(식사, 휴식, 이동 등)은 contentId 를 비워 두고 title 과 description 을 작성하세요.\n");
        prompt.append("   - 목록에 없는 contentId 를 임의로 만들지 마세요.\n");
        prompt.append(outputInstruction());

        return prompt.toString();
    }

    /**
     * 질문별로 답변을 묶어 "질문: 옵션(설명), 옵션" 형태의 줄로 만든다. 질문 순서는 입력 순서를 따른다.
     */
    private static List<String> surveyAnswerLines(List<SurveyAnswerInfo> answers) {
        Map<String, List<String>> byQuestion = new LinkedHashMap<>();
        for (SurveyAnswerInfo answer : answers) {
            String option = answer.optionSubLabel() == null || answer.optionSubLabel().isBlank()
                    ? answer.optionLabel()
                    : answer.optionLabel() + "(" + answer.optionSubLabel() + ")";
            byQuestion.computeIfAbsent(answer.questionTitle(), key -> new ArrayList<>()).add(option);
        }
        return byQuestion.entrySet().stream()
                .map(entry -> "- " + entry.getKey() + ": " + String.join(", ", entry.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * 일별 일정 응답을 Itinerary 엔티티로 조립한다.
     * 프롬프트에 넘긴 관광지(+주축제)만 TOUR 로 연결하고, 그 외 항목은 NORMAL 일정으로 저장한다.
     */
    Itinerary buildItinerary(List<ItineraryDayJson> daysJson, ItineraryGenerateDTO dto) {
        Itinerary itinerary = Itinerary.builder()
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .tour(dto.getTour())
                .userId(dto.getUserId())
                .build();

        Map<String, Tour> knownTours = new HashMap<>();
        dto.getRecommendableTours().forEach(t -> knownTours.put(t.getContentId(), t));
        if (dto.getTour() != null) {
            knownTours.put(dto.getTour().getContentId(), dto.getTour());
        }

        List<ItineraryDay> itineraryDays = new ArrayList<>();
        for (ItineraryDayJson dayJson : daysJson) {
            ItineraryDay day = ItineraryDay.builder()
                    .itinerary(itinerary)
                    .date(LocalDate.parse(dayJson.date()))
                    .dayNumber(dayJson.dayNumber())
                    .itinerarySchedules(new ArrayList<>())
                    .build();

            if (dayJson.items() != null) {
                for (ItineraryTourJson item : dayJson.items()) {
                    ItineraryScheduleMapper.toSchedule(day, item, knownTours::get, dto.getDayStartTime(), dto.getDayEndTime())
                            .ifPresent(day.getItinerarySchedules()::add);
                }
            }
            itineraryDays.add(day);
        }

        itinerary.setItineraryDays(itineraryDays);
        return itinerary;
    }
}
