package com.bosshi.maeul.itinerary.service;

import com.bosshi.maeul.itinerary.entity.Itinerary;
import com.bosshi.maeul.itinerary.entity.ItineraryDay;
import com.bosshi.maeul.itinerary.entity.ItinerarySchedule;
import com.bosshi.maeul.itinerary.entity.ItineraryScheduleType;
import com.bosshi.maeul.itinerary.repository.ItineraryRepository;
import com.bosshi.maeul.itinerary.repository.ItineraryScheduleRepository;
import com.bosshi.maeul.itinerary.repository.ItineraryScheduleTypeRepository;
import com.bosshi.maeul.itinerary.request.ItineraryTourCreateRequest;
import com.bosshi.maeul.itinerary.request.ItineraryTourUpdateRequest;
import com.bosshi.maeul.location.entity.LocationMapping;
import com.bosshi.maeul.location.repository.LocationMappingRepository;
import com.bosshi.maeul.location.type.LocationTargetType;
import com.bosshi.maeul.openapi.entity.Tour;
import com.bosshi.maeul.openapi.repository.TourRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.*;

// enum alias: use fully qualified for the type-package enum to disambiguate
// com.bosshi.maeul.itinerary.type.ItineraryScheduleType → referenced inline below

@Service
@Slf4j
@Transactional
@AllArgsConstructor
public class ItineraryScheduleService {
    private final ItineraryRepository itineraryRepository;
    private final ItineraryScheduleRepository itineraryScheduleRepository;
    private final ItineraryScheduleTypeRepository scheduleTypeRepository;
    private final TourRepository tourRepository;
    private final LocationMappingRepository locationMappingRepository;

    public ItinerarySchedule show(Long itineraryId, Long itineraryDayId, Long id, Long userId) {
        Itinerary itinerary = findOwnedItinerary(itineraryId, userId);
        ItineraryDay day = findDayOrThrow(itinerary, itineraryDayId);
        return findTourOrThrow(day, id);
    }

    public Optional<LocationMapping> getLocation(Long scheduleId) {
        return locationMappingRepository.findByTargetAndTargetId(LocationTargetType.ITINERARY_SCHEDULE, scheduleId);
    }

    public ItinerarySchedule store(Long itineraryId, Long dayId, Long userId, ItineraryTourCreateRequest request) {
        Itinerary itinerary = findOwnedItinerary(itineraryId, userId);
        ItineraryDay day = findDayOrThrow(itinerary, dayId);

        List<ItinerarySchedule> schedules = ensureSchedules(day);

        int sequence = (request.getSequence() != null)
                ? request.getSequence()
                : schedules.stream()
                        .mapToInt(s -> s.getSequence() != null ? s.getSequence() : 0)
                        .max()
                        .orElse(0) + 1;

        validateSequenceUnique(schedules, sequence, null);
        validateTimeRange(request.getStartTime(), request.getEndTime());
        validateNoTimeOverlap(schedules, request.getStartTime(), request.getEndTime(), null);

        String typeCode = request.getType() != null ? request.getType().name() : com.bosshi.maeul.itinerary.type.ItineraryScheduleType.TOUR.name();
        validateByTypeCode(typeCode, request.getContentId(), request.getTitle(), request.getDescription());

        ItineraryScheduleType typeEntity = findScheduleTypeOrThrow(typeCode);

        ItinerarySchedule itinerarySchedule = ItinerarySchedule.builder()
                .itineraryDay(day)
                .type(typeEntity.getCode())
                .sequence(sequence)
                .title(request.getTitle())
                .description(request.getDescription())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .build();

        if (com.bosshi.maeul.itinerary.type.ItineraryScheduleType.TOUR.name().equals(typeCode)) {
            Tour tour = tourRepository.findByContentId(request.getContentId())
                    .orElseThrow(() -> new IllegalArgumentException("관광 정보를 찾을 수 없습니다: " + request.getContentId()));
            itinerarySchedule.setTour(tour);
        }

        ItinerarySchedule saved = itineraryScheduleRepository.save(itinerarySchedule);
        schedules.add(saved);
        schedules.sort(Comparator.comparing(ItinerarySchedule::getSequence, Comparator.nullsLast(Comparator.naturalOrder())));

        return saved;
    }

    public ItinerarySchedule updateTourItem(Long itineraryId, Long dayId, Long itineraryScheduleId, Long userId, ItineraryTourUpdateRequest request) {
        Itinerary itinerary = findOwnedItinerary(itineraryId, userId);
        ItineraryDay day = findDayOrThrow(itinerary, dayId);
        ItinerarySchedule itineraryTour = findTourOrThrow(day, itineraryScheduleId);

        String typeCode = request.getType() != null ? request.getType().name()
                : itineraryTour.getType();

        if (request.getType() != null || request.getContentId() != null
                || request.getTitle() != null || request.getDescription() != null) {
            String contentId = request.getContentId() != null ? request.getContentId()
                    : ("TOUR".equals(typeCode) && itineraryTour.getTour() != null
                            ? itineraryTour.getTour().getContentId() : null);
            String title = request.getTitle() != null ? request.getTitle() : itineraryTour.getTitle();
            String description = request.getDescription() != null ? request.getDescription() : itineraryTour.getDescription();
            validateByTypeCode(typeCode, contentId, title, description);
        }

        if (request.getType() != null) {
            itineraryTour.setType(typeCode);
        }

        if ("TOUR".equals(typeCode) && request.getContentId() != null && !request.getContentId().isBlank()) {
            Tour tour = tourRepository.findByContentId(request.getContentId())
                    .orElseThrow(() -> new IllegalArgumentException("관광 정보를 찾을 수 없습니다: " + request.getContentId()));
            itineraryTour.setTour(tour);
            itineraryTour.setTitle(null);
            itineraryTour.setDescription(null);
        } else if ("NORMAL".equals(typeCode)) {
            if (request.getTitle() != null) itineraryTour.setTitle(request.getTitle());
            if (request.getDescription() != null) itineraryTour.setDescription(request.getDescription());
            itineraryTour.setTour(null);
        }

        if (request.getSequence() != null) {
            validateSequenceUnique(day.getItinerarySchedules(), request.getSequence(), itineraryTour.getId());
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
                day.getItinerarySchedules(),
                itineraryTour.getStartTime(),
                itineraryTour.getEndTime(),
                itineraryTour.getId()
        );

        ensureSchedules(day).sort(Comparator.comparing(ItinerarySchedule::getSequence, Comparator.nullsLast(Comparator.naturalOrder())));
        return itineraryScheduleRepository.save(itineraryTour);
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
        return day.getItinerarySchedules().stream()
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

    private List<ItinerarySchedule> ensureSchedules(ItineraryDay day) {
        if (day.getItinerarySchedules() == null) {
            day.setItinerarySchedules(new ArrayList<>());
        }
        return day.getItinerarySchedules();
    }

    private ItineraryScheduleType findScheduleTypeOrThrow(String code) {
        return scheduleTypeRepository.findById(code)
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 스케줄 타입입니다: " + code));
    }

    private void validateByTypeCode(String typeCode, String contentId, String title, String description) {
        if ("TOUR".equals(typeCode)) {
            if (contentId == null || contentId.isBlank()) {
                throw new IllegalArgumentException("TOUR 타입은 contentId가 필수입니다.");
            }
        } else if ("NORMAL".equals(typeCode)) {
            if (title == null || title.isBlank()) {
                throw new IllegalArgumentException("NORMAL 타입은 title이 필수입니다.");
            }
            if (description == null || description.isBlank()) {
                throw new IllegalArgumentException("NORMAL 타입은 description이 필수입니다.");
            }
        }
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
