package com.bosshi.maeul.itinerary.generator;

import com.bosshi.maeul.itinerary.dto.ItineraryGenerateDTO;
import com.bosshi.maeul.itinerary.entity.Itinerary;
import com.bosshi.maeul.itinerary.entity.ItineraryDay;
import com.bosshi.maeul.itinerary.entity.ItinerarySchedule;
import com.bosshi.maeul.itinerary.generator.impl.ItineraryGeneratorImpl;
import com.bosshi.maeul.openapi.entity.Tour;
import com.bosshi.maeul.openapi.repository.TourRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ItineraryTestGenerator implements ItineraryGeneratorImpl {
    private final TourRepository tourRepository;
    private final ObjectMapper objectMapper;
    private final EntityManager entityManager;

    /**
     * DB에서 랜덤으로 관광지를 조회하여 JSON 문자열 형태의 예시 일정을 반환합니다.
     *
     * @return JSON 포맷의 일정 문자열
     */
    public String getTestResultFromDB(LocalDate startDate, LocalDate endDate) {
        // 2. DB에서 랜덤으로 10개의 Tour 데이터 일괄 조회
        List<Tour> tours = tourRepository.findRandomToursWhereContentTypeIdIs("15", 10);

        if (tours.isEmpty()) {
            return "[]";
        }

        // 3. 응답 데이터를 담을 리스트 (가장 바깥쪽 배열)
        List<Map<String, Object>> itineraryResult = new ArrayList<>();

        int dayCounter = 1;
        int sequenceCounter = 1;
        LocalDate currentDate = startDate;

        List<Map<String, Object>> currentDayItems = new ArrayList<>();

        for (int i = 0; i < tours.size(); i++) {
            Tour tour = tours.get(i);

            // 각 아이템(Tour) 조립
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("contentId", tour.getContentId());
            item.put("sequence", sequenceCounter++);

            currentDayItems.add(item);

            // 샘플 로직: 3개 또는 4개의 일정이 차거나, 마지막 요소일 때 하루(Day) 일정을 완성
            if (currentDayItems.size() >= 3 || i == tours.size() - 1) {
                Map<String, Object> dayNode = new LinkedHashMap<>();
                dayNode.put("date", currentDate.toString()); // 예: "2026-09-01"
                dayNode.put("dayNumber", dayCounter++);
                dayNode.put("items", currentDayItems);

                itineraryResult.add(dayNode);

                // 다음 날짜로 갱신 및 초기화
                currentDate = currentDate.plusDays(1);
                currentDayItems = new ArrayList<>();
                sequenceCounter = 1;
            }
        }

        // 4. Map 구조를 JSON String 형태로 변환
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(itineraryResult);
    }

    @Override
    public Itinerary generateItinerary(ItineraryGenerateDTO dto) throws JsonProcessingException {
        String cleanedJson = getTestResultFromDB(dto.getStartDate(), dto.getEndDate());
        com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

        List<ItineraryGenerator.ItineraryDayJson> daysJson = objectMapper.readValue(
                cleanedJson, new TypeReference<>() {
                }
        );

        Itinerary itinerary = Itinerary.builder()
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .tour(dto.getTour())
                .userId(dto.getUserId())
                .build();

        List<ItineraryDay> itineraryDays = new ArrayList<>();
        for (ItineraryGenerator.ItineraryDayJson dayJson : daysJson) {
            ItineraryDay day = ItineraryDay.builder()
                    .itinerary(itinerary)
                    .date(LocalDate.parse(dayJson.date()))
                    .dayNumber(dayJson.dayNumber())
                    .itineraryTours(new ArrayList<>())
                    .build();

            if (dayJson.items() != null) {
                for (ItineraryGenerator.ItineraryTourJson tourJson : dayJson.items()) {
                    // DB 조회를 하지 않고 프록시(참조) 객체만 생성
                    Tour tourProxy = entityManager.getReference(Tour.class, tourJson.contentId());

                    ItinerarySchedule itineraryTour = ItinerarySchedule.builder()
                            .itineraryDay(day)
                            .tour(tourProxy)
                            .sequence(tourJson.sequence())
                            .build();
                    day.getItineraryTours().add(itineraryTour);
                }
            }
            itineraryDays.add(day);
        }

        itinerary.setItineraryDays(itineraryDays);

        return itinerary;
    }
}