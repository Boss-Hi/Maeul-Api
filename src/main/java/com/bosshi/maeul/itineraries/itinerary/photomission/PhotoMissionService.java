package com.bosshi.maeul.itineraries.itinerary.photomission;

import com.bosshi.maeul.ai.AiImage;
import com.bosshi.maeul.common.exception.ServiceUnavailableException;
import com.bosshi.maeul.gacha.GachaTicketIssuer;
import com.bosshi.maeul.gacha.TicketEventType;
import com.bosshi.maeul.itineraries.itinerary.entity.Itinerary;
import com.bosshi.maeul.itineraries.itinerary.entity.ItineraryDay;
import com.bosshi.maeul.itineraries.itinerary.entity.ItinerarySchedule;
import com.bosshi.maeul.itineraries.itinerary.event.MissionCompletedEvent;
import com.bosshi.maeul.itineraries.itinerary.repository.ItineraryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 사진 미션 (ADR 0017). 앵커 일정에 장소의 기준 사진을 붙이고, 사용자가 같은 곳을 찍어 올리면 AI 로 판정해
 * 가챠권 1장과 미션 완료(펫 경험치 가챠)를 준다. 올린 사진은 저장하지 않는다.
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class PhotoMissionService {

    /**
     * 사진 미션 완료를 MissionCompletedEvent 로 보낼 때 쓰는 ID 에 더하는 값.
     * 이벤트의 미션 ID 는 itinerary_mission_mappings.id 와 같은 번호 공간이라 겹치지 않게 큰 값을 더한다.
     */
    static final long EVENT_ID_OFFSET = 1_000_000_000_000L;

    private final PhotoMissionRepository missionRepository;
    private final PhotoMissionPlaceRepository placeRepository;
    private final PhotoMissionReferencePhotoRepository referencePhotoRepository;
    private final PhotoMissionAttemptRepository attemptRepository;
    private final ItineraryRepository itineraryRepository;
    private final ReferenceImageFetcher fetcher;
    private final PhotoJudge judge;
    private final GachaTicketIssuer ticketIssuer;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;
    private final ZoneId zone;
    private final Random random;
    private final double minConfidence;
    private final int maxAttemptsPerDay;
    private final Duration exifMaxGap;
    private final int maxImageBytes;

    @Autowired
    public PhotoMissionService(PhotoMissionRepository missionRepository, PhotoMissionPlaceRepository placeRepository,
                               PhotoMissionReferencePhotoRepository referencePhotoRepository,
                               PhotoMissionAttemptRepository attemptRepository, ItineraryRepository itineraryRepository,
                               ReferenceImageFetcher fetcher, PhotoJudge judge, GachaTicketIssuer ticketIssuer,
                               ApplicationEventPublisher eventPublisher,
                               @Value("${timeline.zone:Asia/Seoul}") String zone,
                               @Value("${photo-mission.min-confidence:0.7}") double minConfidence,
                               @Value("${photo-mission.max-attempts-per-day:5}") int maxAttemptsPerDay,
                               @Value("${photo-mission.exif-max-minutes:30}") int exifMaxMinutes,
                               @Value("${photo-mission.max-image-bytes:5242880}") int maxImageBytes) {
        this(missionRepository, placeRepository, referencePhotoRepository, attemptRepository, itineraryRepository, fetcher, judge,
                ticketIssuer, eventPublisher, Clock.system(ZoneId.of(zone)), new Random(), minConfidence, maxAttemptsPerDay,
                Duration.ofMinutes(exifMaxMinutes), maxImageBytes);
    }

    PhotoMissionService(PhotoMissionRepository missionRepository, PhotoMissionPlaceRepository placeRepository,
                        PhotoMissionReferencePhotoRepository referencePhotoRepository,
                        PhotoMissionAttemptRepository attemptRepository, ItineraryRepository itineraryRepository,
                        ReferenceImageFetcher fetcher, PhotoJudge judge, GachaTicketIssuer ticketIssuer,
                        ApplicationEventPublisher eventPublisher, Clock clock, Random random, double minConfidence,
                        int maxAttemptsPerDay, Duration exifMaxGap, int maxImageBytes) {
        this.missionRepository = missionRepository;
        this.placeRepository = placeRepository;
        this.referencePhotoRepository = referencePhotoRepository;
        this.attemptRepository = attemptRepository;
        this.itineraryRepository = itineraryRepository;
        this.fetcher = fetcher;
        this.judge = judge;
        this.ticketIssuer = ticketIssuer;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
        this.zone = clock.getZone();
        this.random = random;
        this.minConfidence = minConfidence;
        this.maxAttemptsPerDay = maxAttemptsPerDay;
        this.exifMaxGap = exifMaxGap;
        this.maxImageBytes = maxImageBytes;
    }

    /** 진행 중 일정에 붙은 사진 미션과 오늘 남은 시도 횟수 */
    public record MissionView(PhotoMission mission, PhotoMissionPlace place, List<ReferencePhoto> referencePhotos, int remainingAttempts) {
    }

    /** 퀴즈로 보여 줄 기준 사진 하나. imageUrl 은 서버가 서빙하는 경로이거나 운영자가 등록한 URL 이다 */
    public record ReferencePhoto(Long id, String imageUrl) {
    }

    /** 서버가 서빙하는 기준 사진 */
    public record ReferenceImage(byte[] data, String mimeType) {
    }

    static final String REFERENCE_IMAGE_PATH = "/api/photo-missions/reference-photos/";

    /**
     * 제출 결과. 사진이 같은 장소로 판정되지 않은 것은 오류가 아니라 실패 결과다.
     *
     * @param ticketIssued 가챠권을 발급했는가 (같은 시군구에 기간이 겹치는 일정은 한 번만 준다)
     */
    public record SubmitResult(boolean success, AttemptResult result, int remainingAttempts, boolean ticketIssued) {
    }

    /**
     * 일정에 사진 미션을 붙인다. 일정 항목 제목에 장소 키워드가 있고 기준 사진이 있는 장소만 붙이며,
     * 이미 붙은 미션은 기간만 일정에 맞춘다.
     */
    @Transactional
    public void attach(Long itineraryId, Long userId, String regionCode, String subdivisionCode, LocalDate startDate, LocalDate endDate) {
        Itinerary itinerary = itineraryRepository.findById(itineraryId).orElse(null);
        if (itinerary == null || itinerary.getItineraryDays() == null) {
            return;
        }
        List<String> titles = itinerary.getItineraryDays().stream()
                .map(ItineraryDay::getItinerarySchedules)
                .filter(java.util.Objects::nonNull)
                .flatMap(List::stream)
                .map(ItinerarySchedule::getTitle)
                .filter(java.util.Objects::nonNull)
                .toList();
        for (PhotoMissionPlace place : placeRepository.findByEnabledTrueAndDeletedAtIsNull()) {
            if (titles.stream().noneMatch(title -> title.contains(place.getKeyword()))) {
                continue;
            }
            if (missionRepository.existsByItineraryIdAndPlaceId(itineraryId, place.getId())) {
                continue;
            }
            if (referencePhotoRepository.findEnabledSummaries(place.getId()).isEmpty()) {
                log.warn("기준 사진이 없어 사진 미션을 붙이지 않습니다: place={}", place.getKeyword());
                continue;
            }
            missionRepository.save(PhotoMission.builder()
                    .itineraryId(itineraryId)
                    .userId(userId)
                    .placeId(place.getId())
                    .regionCode(regionCode)
                    .subdivisionCode(subdivisionCode)
                    .startDate(startDate)
                    .endDate(endDate)
                    .build());
        }
        syncPeriod(itineraryId, startDate, endDate);
    }

    private void syncPeriod(Long itineraryId, LocalDate startDate, LocalDate endDate) {
        for (PhotoMission mission : missionRepository.findByItineraryIdInAndDeletedAtIsNull(List.of(itineraryId))) {
            if (!mission.getStartDate().equals(startDate) || !mission.getEndDate().equals(endDate)) {
                mission.setStartDate(startDate);
                mission.setEndDate(endDate);
                missionRepository.save(mission);
            }
        }
    }

    public List<MissionView> current(Long userId) {
        LocalDate today = LocalDate.now(clock);
        Optional<Itinerary> itinerary = itineraryRepository.findFirstByUserIdAndEndDateGreaterThanEqualOrderByStartDateDesc(userId, today);
        if (itinerary.isEmpty()) {
            return List.of();
        }
        List<PhotoMission> missions = missionRepository.findByItineraryIdInAndDeletedAtIsNull(List.of(itinerary.get().getId()));
        Map<Long, PhotoMissionPlace> places = placeRepository.findAllById(missions.stream().map(PhotoMission::getPlaceId).toList()).stream()
                .collect(Collectors.toMap(PhotoMissionPlace::getId, Function.identity()));
        return missions.stream()
                .map(mission -> new MissionView(mission, places.get(mission.getPlaceId()),
                        referencePhotos(mission.getPlaceId()), remaining(mission, today)))
                .toList();
    }

    private List<ReferencePhoto> referencePhotos(Long placeId) {
        return referencePhotoRepository.findEnabledSummaries(placeId).stream()
                .map(summary -> new ReferencePhoto(summary.id(), summary.hasData() ? REFERENCE_IMAGE_PATH + summary.id() + "/image" : summary.imageUrl()))
                .toList();
    }

    /** 서버가 서빙하는 기준 사진. 사용 중이고 이미지 본문이 있는 것만 */
    public Optional<ReferenceImage> referenceImage(Long referencePhotoId) {
        return referencePhotoRepository.findById(referencePhotoId)
                .filter(photo -> photo.isEnabled() && !photo.isDeleted() && photo.getImageBase64() != null)
                .map(photo -> new ReferenceImage(Base64.getDecoder().decode(photo.getImageBase64()), photo.getMimeType()));
    }

    /**
     * 사진을 제출한다. 일정 기간 안이고 장소 반경 안일 때만 받으며, 같은 사진을 다시 올리면 거절한다.
     * AI 가 답하지 못하면 시도로 세지 않고 {@link com.bosshi.maeul.common.exception.ServiceUnavailableException} 을 던진다.
     *
     * @throws IllegalArgumentException 제출할 수 없는 상태나 입력일 때
     */
    @Transactional
    public SubmitResult submit(Long userId, Long photoMissionId, byte[] image, double lat, double lng) {
        PhotoMission mission = missionRepository.findById(photoMissionId)
                .filter(m -> m.getUserId().equals(userId) && !m.isDeleted())
                .orElseThrow(() -> new IllegalArgumentException("사진 미션을 찾을 수 없습니다: " + photoMissionId));
        if (mission.getStatus() == PhotoMissionStatus.COMPLETE) {
            throw new IllegalArgumentException("이미 완료한 사진 미션입니다.");
        }
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDate today = now.toLocalDate();
        if (!PhotoMissionRules.withinPeriod(mission.getStartDate(), mission.getEndDate(), today)) {
            throw new IllegalArgumentException("일정 기간에만 도전할 수 있습니다.");
        }
        PhotoMissionPlace place = placeRepository.findById(mission.getPlaceId())
                .orElseThrow(() -> new IllegalStateException("사진 미션 장소가 없습니다: " + mission.getPlaceId()));
        double distance = PhotoMissionRules.distanceMeters(lat, lng, place.getLatitude(), place.getLongitude());
        if (distance > place.getRadiusM()) {
            throw new IllegalArgumentException("미션 장소 근처에서 촬영해 주세요.");
        }
        if (image == null || image.length == 0 || image.length > maxImageBytes) {
            throw new IllegalArgumentException("사진은 " + (maxImageBytes / 1024 / 1024) + "MB 이하여야 합니다.");
        }
        String mime = PhotoMissionRules.detectMime(image);
        if (mime == null) {
            throw new IllegalArgumentException("JPEG, PNG, WebP 사진만 올릴 수 있습니다.");
        }
        String hash = sha256(image);
        if (attemptRepository.existsByUserIdAndImageHash(userId, hash)) {
            throw new IllegalArgumentException("이미 올린 사진입니다. 새로 촬영해 주세요.");
        }
        long used = attemptRepository.countByPhotoMissionIdAndAttemptedOn(mission.getId(), today);
        if (used >= maxAttemptsPerDay) {
            throw new IllegalArgumentException("오늘 시도 횟수를 모두 사용했습니다. 내일 다시 도전해 주세요.");
        }
        int remainingAfter = (int) (maxAttemptsPerDay - used - 1);

        Optional<LocalDateTime> takenAt = ImagePreparer.takenAt(image, zone);
        if (takenAt.isPresent() && PhotoMissionRules.isStale(takenAt.get(), now, exifMaxGap)) {
            saveAttempt(mission, today, false, lat, lng, distance, hash, AttemptResult.EXIF_STALE, null);
            return new SubmitResult(false, AttemptResult.EXIF_STALE, remainingAfter, false);
        }

        List<AiImage> references = referenceImages(mission.getPlaceId());
        PhotoJudge.Verdict verdict = judge.judge(references, ImagePreparer.prepare(image, mime));
        PhotoJudgement judgement = verdict.judgement();
        AttemptResult result = !judgement.same() ? AttemptResult.NOT_SAME
                : judgement.confidence() < minConfidence ? AttemptResult.LOW_CONFIDENCE : AttemptResult.SUCCESS;
        saveAttempt(mission, today, result == AttemptResult.SUCCESS, lat, lng, distance, hash, result, verdict);
        if (result != AttemptResult.SUCCESS) {
            return new SubmitResult(false, result, remainingAfter, false);
        }

        boolean reward = mission.getRegionCode() == null || mission.getSubdivisionCode() == null
                || !missionRepository.existsRewardedOverlap(userId, mission.getRegionCode(), mission.getSubdivisionCode(),
                mission.getStartDate(), mission.getEndDate(), mission.getId());
        if (missionRepository.markComplete(mission.getId(), now, reward) == 0) {
            throw new IllegalArgumentException("이미 완료한 사진 미션입니다.");
        }
        if (reward) {
            ticketIssuer.issue(userId, TicketEventType.PHOTO_MISSION, "photo-mission:" + mission.getId());
        }
        eventPublisher.publishEvent(new MissionCompletedEvent(userId, EVENT_ID_OFFSET + mission.getId(),
                mission.getRegionCode(), mission.getSubdivisionCode()));
        return new SubmitResult(true, AttemptResult.SUCCESS, remainingAfter, reward);
    }

    /** 장소의 사용 중인 기준 사진 전체. 하나라도 맞으면 성공이므로 모두 AI 에 보낸다 */
    private List<AiImage> referenceImages(Long placeId) {
        List<PhotoMissionReferencePhoto> photos = referencePhotoRepository.findByPlaceIdAndEnabledTrueAndDeletedAtIsNullOrderByIdAsc(placeId);
        if (photos.isEmpty()) {
            throw new ServiceUnavailableException(PhotoMissionRules.UNAVAILABLE_MESSAGE);
        }
        List<AiImage> images = new ArrayList<>();
        for (PhotoMissionReferencePhoto photo : photos) {
            AiImage source = photo.getImageBase64() != null
                    ? new AiImage(Base64.getDecoder().decode(photo.getImageBase64()), photo.getMimeType())
                    : fetcher.fetch(photo.getImageUrl());
            images.add(ImagePreparer.prepare(source.data(), source.mimeType()));
        }
        return images;
    }

    private int remaining(PhotoMission mission, LocalDate today) {
        return (int) Math.max(0, maxAttemptsPerDay - attemptRepository.countByPhotoMissionIdAndAttemptedOn(mission.getId(), today));
    }

    private void saveAttempt(PhotoMission mission, LocalDate today, boolean success, double lat, double lng, double distance,
                             String hash, AttemptResult result, PhotoJudge.Verdict verdict) {
        PhotoJudgement judgement = verdict == null ? null : verdict.judgement();
        attemptRepository.save(PhotoMissionAttempt.builder()
                .photoMissionId(mission.getId())
                .userId(mission.getUserId())
                .attemptedOn(today)
                .success(success)
                .latitude(lat)
                .longitude(lng)
                .distanceM(distance)
                .imageHash(hash)
                .result(result)
                .aiProvider(verdict == null ? null : verdict.provider())
                .aiSame(judgement == null ? null : judgement.same())
                .aiConfidence(judgement == null ? null : judgement.confidence())
                .aiReason(judgement == null || judgement.reason() == null ? null
                        : judgement.reason().substring(0, Math.min(judgement.reason().length(), 1000)))
                .build());
    }

    static String sha256(byte[] data) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(data));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
