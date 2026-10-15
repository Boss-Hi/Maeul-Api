package com.bosshi.maeul.itinerary.service;

import com.bosshi.maeul.itinerary.dto.ItineraryGenerateDTO;
import com.bosshi.maeul.itinerary.entity.Itinerary;
import com.bosshi.maeul.itinerary.entity.ItineraryDay;
import com.bosshi.maeul.itinerary.entity.ItineraryTour;
import com.bosshi.maeul.itinerary.filter.TourFilteringService;
import com.bosshi.maeul.itinerary.generator.impl.ItineraryGeneratorImpl;
import com.bosshi.maeul.itinerary.request.ItineraryDayCreateRequest;
import com.bosshi.maeul.itinerary.request.ItineraryDayUpdateRequest;
import com.bosshi.maeul.itinerary.request.ItineraryTourCreateRequest;
import com.bosshi.maeul.itinerary.request.ItineraryTourUpdateRequest;
import com.bosshi.maeul.itinerary.request.ItineraryUpdateRequest;
import com.bosshi.maeul.itinerary.repository.ItineraryRepository;
import com.bosshi.maeul.openapi.entity.Tour;
import com.bosshi.maeul.openapi.repository.TourRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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
public class ItineraryService {
    private final TourRepository tourRepository;
    private final TourFilteringService tourFilteringService;
    private final ItineraryRepository itineraryRepository;
    private final ItineraryGeneratorImpl itineraryGenerator;

    // @RequiredArgsConstructor 대신 직접 생성자를 작성하고 파라미터에 @Qualifier 적용
    public ItineraryService(
            TourRepository tourRepository,
            TourFilteringService tourFilteringService,
            ItineraryRepository itineraryRepository,
            @Qualifier("itineraryTestGenerator") ItineraryGeneratorImpl itineraryGenerator) {

        this.tourRepository = tourRepository;
        this.tourFilteringService = tourFilteringService;
        this.itineraryRepository = itineraryRepository;
        this.itineraryGenerator = itineraryGenerator;
    }
    /**
     * 사용자가 MainFestival을 선택하고 일정 생성을 요청합니다.
     *
     * @param dto MainFestival 선택 요청
     */
    public Itinerary generateItinerary(ItineraryGenerateDTO dto) throws JsonProcessingException {
        Tour tour = tourRepository.findByContentId(dto.getTourId())
                .orElseThrow(() -> new IllegalArgumentException("관광 정보를 찾을 수 없습니다: " + dto.getTourId()));
        dto.setTour(tour);
        dto.setRecommendableTours(tourFilteringService.filterTourByCategories(dto));

        Itinerary itinerary = itineraryGenerator.generateItinerary(dto);
        Itinerary savedItinerary = itineraryRepository.save(itinerary);
        log.info(
                "응답 파싱 및 DB 저장 완료: ID={}, 일수={}", savedItinerary.getId(), savedItinerary.getItineraryDays()
                        .size()
        );
        return savedItinerary;
    }

    public Itinerary findById(Long id) {
        return itineraryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("일정을 찾을 수 없습니다: "));
    }

    public Itinerary getCurrent(Long userId) {
        return itineraryRepository.findFirstByUserIdAndEndDateGreaterThanEqualOrderByStartDateDesc(userId, LocalDate.now())
                .orElseThrow(() -> new IllegalArgumentException("일정을 찾을 수 없습니다: "));
    }

    public Itinerary updateItinerary(Long itineraryId, Long userId, ItineraryUpdateRequest request) {
        Itinerary itinerary = findOwnedItinerary(itineraryId, userId);

        if (request.getStartDate() != null) {
            itinerary.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            itinerary.setEndDate(request.getEndDate());
        }
        validateDateRange(itinerary.getStartDate(), itinerary.getEndDate());

        if (request.getStatus() != null) {
            itinerary.setStatus(request.getStatus());
        }
        if (request.getTourId() != null) {
            Tour tour = tourRepository.findByContentId(request.getTourId())
                    .orElseThrow(() -> new IllegalArgumentException("관광 정보를 찾을 수 없습니다: " + request.getTourId()));
            itinerary.setTour(tour);
        }

        return itineraryRepository.save(itinerary);
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

        ItineraryTour itineraryTour = ItineraryTour.builder()
                .itineraryDay(day)
                .tour(tour)
                .sequence(request.getSequence())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .build();

        day.getItineraryTours().add(itineraryTour);
        day.getItineraryTours().sort(Comparator.comparing(ItineraryTour::getSequence));

        return itineraryRepository.save(itinerary);
    }

    public Itinerary updateTourItem(Long itineraryId, Long dayId, Long itineraryTourId, Long userId, ItineraryTourUpdateRequest request) {
        Itinerary itinerary = findOwnedItinerary(itineraryId, userId);
        ItineraryDay day = findDayOrThrow(itinerary, dayId);
        ItineraryTour itineraryTour = findTourOrThrow(day, itineraryTourId);

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

        day.getItineraryTours().sort(Comparator.comparing(ItineraryTour::getSequence));
        return itineraryRepository.save(itinerary);
    }

    private Itinerary findOwnedItinerary(Long itineraryId, Long userId) {
        Itinerary itinerary = findById(itineraryId);
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

    private ItineraryTour findTourOrThrow(ItineraryDay day, Long itineraryTourId) {
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

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("startDate와 endDate는 필수입니다.");
        }
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("endDate는 startDate보다 빠를 수 없습니다.");
        }
    }

    private void validateDayNumberUnique(List<ItineraryDay> days, Integer dayNumber, Long excludeDayId) {
        boolean exists = days.stream()
                .anyMatch(day -> day.getDayNumber().equals(dayNumber) && !Objects.equals(day.getId(), excludeDayId));
        if (exists) {
            throw new IllegalArgumentException("동일한 dayNumber가 이미 존재합니다: " + dayNumber);
        }
    }

    private void validateSequenceUnique(List<ItineraryTour> tours, Integer sequence, Long excludeTourId) {
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

    private void validateNoTimeOverlap(List<ItineraryTour> tours, LocalTime startTime, LocalTime endTime, Long excludeTourId) {
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

    private boolean isTimeOverlapped(LocalTime startA, LocalTime endA, LocalTime startB, LocalTime endB) {
        return startA.isBefore(endB) && startB.isBefore(endA);
    }
}

