package com.bosshi.maeul.itinerary.service;

import com.bosshi.maeul.itinerary.entity.Itinerary;
import com.bosshi.maeul.itinerary.entity.ItineraryDay;
import com.bosshi.maeul.itinerary.entity.ItinerarySchedule;
import com.bosshi.maeul.itinerary.repository.ItineraryRepository;
import com.bosshi.maeul.itinerary.repository.ItineraryScheduleRepository;
import com.bosshi.maeul.itinerary.request.ItineraryTourCreateRequest;
import com.bosshi.maeul.itinerary.request.ItineraryTourUpdateRequest;
import com.bosshi.maeul.openapi.entity.Tour;
import com.bosshi.maeul.openapi.repository.TourRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

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
public class ItineraryScheduleService {
    private final ItineraryRepository itineraryRepository;
    private final ItineraryScheduleRepository itineraryScheduleRepository;
    private final TourRepository tourRepository;

    public ItinerarySchedule show(Long itineraryId, Long id, Long userId) {
        Itinerary itinerary = findOwnedItinerary(itineraryId, userId);
        return findTourOrThrow(itinerary, id);
    }

    public Itinerary addTourItem(Long itineraryId, Long dayId, Long userId, ItineraryTourCreateRequest request) {
        Itinerary itinerary = findOwnedItinerary(itineraryId, userId);
        ItineraryDay day = findDayOrThrow(itinerary, dayId);

        if (request.getContentId() == null || request.getContentId().isBlank()) {
            throw new IllegalArgumentException("contentId는 필수입니다.");
        }
        if (request.getSequence() == null) {
            throw new IllegalArgumentException("sequence는 필수입니다.");
        }

        Tour tour = tourRepository.findByContentId(request.getContentId())
                .orElseThrow(() -> new IllegalArgumentException("관광 정보를 찾을 수 없습니다: " + request.getContentId()));

        validateSequenceUnique(day.getItineraryTours(), request.getSequence(), null);
        validateTimeRange(request.getStartTime(), request.getEndTime());
        validateNoTimeOverlap(day.getItineraryTours(), request.getStartTime(), request.getEndTime(), null);

        ItinerarySchedule itineraryTour = ItinerarySchedule.builder()
                .itineraryDay(day)
                .tour(tour)
                .sequence(request.getSequence())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .build();

        day.getItineraryTours().add(itineraryTour);
        day.getItineraryTours().sort(Comparator.comparing(ItinerarySchedule::getSequence));

        return itineraryRepository.save(itinerary);
    }

    public Itinerary updateTourItem(Long itineraryId, Long dayId, Long itineraryTourId, Long userId, ItineraryTourUpdateRequest request) {
        Itinerary itinerary = findOwnedItinerary(itineraryId, userId);
        ItineraryDay day = findDayOrThrow(itinerary, dayId);
        ItinerarySchedule itineraryTour = findTourOrThrow(day, itineraryTourId);

        if (request.getContentId() != null && !request.getContentId().isBlank()) {
            Tour tour = tourRepository.findByContentId(request.getContentId())
                    .orElseThrow(() -> new IllegalArgumentException("관광 정보를 찾을 수 없습니다: " + request.getContentId()));
            itineraryTour.setTour(tour);
        }
        if (request.getSequence() != null) {
            validateSequenceUnique(day.getItineraryTours(), request.getSequence(), itineraryTour.getId());
            itineraryTour.setSequence(request.getSequence());
        }

        if (Boolean.TRUE.equals(request.getClearTime())) {
            itineraryTour.setStartTime(null);
            itineraryTour.setEndTime(null);
        } else if (request.getStartTime() != null || request.getEndTime() != null) {
            validateTimeRange(request.getStartTime(), request.getEndTime());
            itineraryTour.setStartTime(request.getStartTime());
            itineraryTour.setEndTime(request.getEndTime());
        }

        validateNoTimeOverlap(
                day.getItineraryTours(),
                itineraryTour.getStartTime(),
                itineraryTour.getEndTime(),
                itineraryTour.getId()
        );

        day.getItineraryTours().sort(Comparator.comparing(ItinerarySchedule::getSequence));
        return itineraryRepository.save(itinerary);
    }

    public Itinerary findItineraryById(Long id) {
        return itineraryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("일정을 찾을 수 없습니다: "));
    }

    private Itinerary findOwnedItinerary(Long itineraryId, Long userId) {
        Itinerary itinerary = findItineraryById(itineraryId);
        if (!itinerary.getUserId().equals(userId)) {
            throw new IllegalArgumentException("해당 일정에 대한 수정 권한이 없습니다.");
        }
        return itinerary;
    }

    private ItineraryDay findDayOrThrow(Itinerary itinerary, Long dayId) {
        return ensureDays(itinerary).stream()
                .filter(day -> day.getId().equals(dayId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("일차 정보를 찾을 수 없습니다: " + dayId));
    }

    private ItinerarySchedule findTourOrThrow(ItineraryDay day, Long itineraryTourId) {
        return day.getItineraryTours().stream()
                .filter(tour -> tour.getId().equals(itineraryTourId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("일정 항목을 찾을 수 없습니다: " + itineraryTourId));
    }

    private List<ItineraryDay> ensureDays(Itinerary itinerary) {
        if (itinerary.getItineraryDays() == null) {
            itinerary.setItineraryDays(new ArrayList<>());
        }
        return itinerary.getItineraryDays();
    }

    private void validateDayNumberUnique(List<ItineraryDay> days, Integer dayNumber, Long excludeDayId) {
        boolean exists = days.stream()
                .anyMatch(day -> day.getDayNumber().equals(dayNumber) && !Objects.equals(day.getId(), excludeDayId));
        if (exists) {
            throw new IllegalArgumentException("동일한 dayNumber가 이미 존재합니다: " + dayNumber);
        }
    }

    private void validateSequenceUnique(List<ItinerarySchedule> tours, Integer sequence, Long excludeTourId) {
        boolean exists = tours.stream()
                .anyMatch(tour -> tour.getSequence().equals(sequence) && !Objects.equals(tour.getId(), excludeTourId));
        if (exists) {
            throw new IllegalArgumentException("동일한 sequence가 이미 존재합니다: " + sequence);
        }
    }

    private void validateTimeRange(LocalTime startTime, LocalTime endTime) {
        if (startTime == null && endTime == null) {
            return;
        }
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("시작 시간과 종료 시간은 함께 입력해야 합니다.");
        }
        if (!startTime.isBefore(endTime)) {
            throw new IllegalArgumentException("종료 시간은 시작 시간보다 늦어야 합니다.");
        }
    }

    private void validateNoTimeOverlap(List<ItinerarySchedule> tours, LocalTime startTime, LocalTime endTime, Long excludeTourId) {
        if (startTime == null || endTime == null) {
            return;
        }
        boolean overlapped = tours.stream()
                .filter(tour -> !Objects.equals(tour.getId(), excludeTourId))
                .filter(tour -> tour.getStartTime() != null && tour.getEndTime() != null)
                .anyMatch(tour -> isTimeOverlapped(startTime, endTime, tour.getStartTime(), tour.getEndTime()));
        if (overlapped) {
            throw new IllegalArgumentException("같은 일차 내 겹치는 시간대가 존재합니다.");
        }
    }

    private boolean isTimeOverlapped(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
        return !start1.isAfter(end2) && !start2.isAfter(end1);
    }
}
