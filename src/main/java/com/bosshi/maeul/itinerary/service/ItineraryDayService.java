package com.bosshi.maeul.itinerary.service;

import com.bosshi.maeul.itinerary.entity.Itinerary;
import com.bosshi.maeul.itinerary.entity.ItineraryDay;
import com.bosshi.maeul.itinerary.repository.ItineraryDayRepository;
import com.bosshi.maeul.itinerary.repository.ItineraryRepository;
import com.bosshi.maeul.itinerary.request.ItineraryDayCreateRequest;
import com.bosshi.maeul.itinerary.request.ItineraryDayUpdateRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 여행 추천 시스템의 핵심 조율 서비스
 * <p>
 * 전체 여행 일정 추천 프로세스를 관리합니다:
 * 1. Trip 생성
 * 2. MainFestival 선택
 * 3. 관광지 필터링
 * 4. AI 일정 생성
 */
@Service
@Slf4j
@Transactional
@AllArgsConstructor
public class ItineraryDayService {
    private final ItineraryRepository itineraryRepository;
    private final ItineraryDayRepository itineraryDayRepository;

    public ItineraryDay show(Long itineraryId, Long id, Long userId) {
        Itinerary itinerary = findOwnedItinerary(itineraryId, userId);
        return findDayOrThrow(itinerary, id);
    }

    public Itinerary addDay(Long itineraryId, Long userId, ItineraryDayCreateRequest request) {
        Itinerary itinerary = findOwnedItinerary(itineraryId, userId);

        if (request.getDate() == null || request.getDayNumber() == null) {
            throw new IllegalArgumentException("date와 dayNumber는 필수입니다.");
        }

        List<ItineraryDay> days = ensureDays(itinerary);
        validateDayNumberUnique(days, request.getDayNumber(), null);

        ItineraryDay day = ItineraryDay.builder()
                .itinerary(itinerary)
                .date(request.getDate())
                .dayNumber(request.getDayNumber())
                .itineraryTours(new ArrayList<>())
                .build();

        days.add(day);
        days.sort(Comparator.comparing(ItineraryDay::getDayNumber));
        return itineraryRepository.save(itinerary);
    }

    public Itinerary updateDay(Long itineraryId, Long dayId, Long userId, ItineraryDayUpdateRequest request) {
        Itinerary itinerary = findOwnedItinerary(itineraryId, userId);
        ItineraryDay day = findDayOrThrow(itinerary, dayId);

        if (request.getDate() != null) {
            day.setDate(request.getDate());
        }
        if (request.getDayNumber() != null) {
            validateDayNumberUnique(ensureDays(itinerary), request.getDayNumber(), day.getId());
            day.setDayNumber(request.getDayNumber());
        }

        ensureDays(itinerary).sort(Comparator.comparing(ItineraryDay::getDayNumber));
        return itineraryRepository.save(itinerary);
    }

    private Itinerary findOwnedItinerary(Long itineraryId, Long userId) {
        Itinerary itinerary = findItineraryById(itineraryId);
        if (!itinerary.getUserId().equals(userId)) {
            throw new IllegalArgumentException("해당 일정에 대한 수정 권한이 없습니다.");
        }
        return itinerary;
    }

    private Itinerary findItineraryById(Long itineraryId) {
        return itineraryRepository.findById(itineraryId)
                .orElseThrow(() -> new IllegalArgumentException("일정을 찾을 수 없습니다: " + itineraryId));
    }

    private ItineraryDay findDayOrThrow(Itinerary itinerary, Long dayId) {
        return itinerary.getItineraryDays().stream()
                .filter(day -> day.getId().equals(dayId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("일정을 찾을 수 없습니다: " + dayId));
    }

    private List<ItineraryDay> ensureDays(Itinerary itinerary) {
        if (itinerary.getItineraryDays() == null) {
            itinerary.setItineraryDays(new ArrayList<>());
        }
        return itinerary.getItineraryDays();
    }

    private void validateDayNumberUnique(List<ItineraryDay> days, Integer dayNumber, Long excludeId) {
        for (ItineraryDay day : days) {
            if (day.getDayNumber().equals(dayNumber) && (excludeId == null || !day.getId().equals(excludeId))) {
                throw new IllegalArgumentException("중복된 dayNumber가 존재합니다: " + dayNumber);
            }
        }
    }

}
