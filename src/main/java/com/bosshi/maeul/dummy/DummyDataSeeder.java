package com.bosshi.maeul.dummy;

import com.bosshi.maeul.itineraries.itinerary.entity.Itinerary;
import com.bosshi.maeul.itineraries.itinerary.entity.ItineraryDay;
import com.bosshi.maeul.itineraries.itinerary.entity.ItinerarySchedule;
import com.bosshi.maeul.itineraries.itinerary.event.ItineraryPeriodChangedEvent;
import com.bosshi.maeul.itineraries.itinerary.repository.ItineraryRepository;
import com.bosshi.maeul.itineraries.itinerary.type.ItineraryStatus;
import com.bosshi.maeul.itineraries.openapi.entity.Tour;
import com.bosshi.maeul.territory.dto.FatRegion;
import com.bosshi.maeul.territory.entity.Fat;
import com.bosshi.maeul.territory.repository.FatRepository;
import com.bosshi.maeul.territory.service.FatService;
import com.bosshi.maeul.timeline.config.TimelineProperties;
import com.bosshi.maeul.timeline.request.TimelinePointsRequest;
import com.bosshi.maeul.timeline.service.TimelineService;
import com.bosshi.maeul.user.entity.Profile;
import com.bosshi.maeul.user.entity.User;
import com.bosshi.maeul.user.repository.UserRepository;
import com.bosshi.maeul.user.type.UserRole;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 개발용 임시 데이터. dummy 프로필로 띄우면 한 번만 만든다 (dummy001@maeul.local 이 있으면 건너뜀).
 * <p>
 * 임시 유저 100명마다 지난 일정(1~3일, 10명은 8일), 진행 중 일정(일부), 다가오는 일정(일부)을 만들고,
 * 지난·진행 중 일정 기간에는 그 일정대로 움직인 1분 간격 위치 기록을 실제 업로드 흐름(TimelineService)으로 넣는다.
 * 위치 기록이 들어가면 점령 시간, 포인트, 펫 경험치는 앱이 계산한다. 펫은 일정 시작 전에 분양받은 것으로 만들고,
 * 8일 일정을 다녀온 유저는 건국 레벨에 이르면 졸업시킨다.
 * <p>
 * 실행: ./gradlew bootRun --args='--spring.profiles.active=dummy'
 */
@Slf4j
@Component
@org.springframework.context.annotation.Profile("dummy")
@Order(100)
@RequiredArgsConstructor
public class DummyDataSeeder implements CommandLineRunner {

    private static final int USER_COUNT = 100;
    private static final int FESTIVAL_COUNT = 25;
    private static final double NEARBY_KM = 5.0;
    private static final String EMAIL_FORMAT = "dummy%03d@maeul.local";
    private static final String PASSWORD = "dummy1234!";

    private static final String[] SURNAMES = {"김", "이", "박", "최", "정", "강", "조", "윤", "장", "임", "한", "오", "서", "신", "권"};
    private static final String[] GIVEN_NAMES = {"민준", "서연", "도윤", "하은", "지호", "수아", "예준", "지유", "현우", "채원", "준서", "다은", "건우", "소율", "우진", "지민"};
    private static final String[] ADJECTIVES = {"느긋한", "부지런한", "배고픈", "신나는", "조용한", "용감한", "졸린", "명랑한"};
    private static final String[] NOUNS = {"여행자", "산책러", "미식가", "탐험가", "축제왕", "길잡이"};

    private final UserRepository userRepository;
    private final ItineraryRepository itineraryRepository;
    private final FatRepository fatRepository;
    private final FatService fatService;
    private final TimelineService timelineService;
    private final TimelineProperties timelineProperties;
    private final ApplicationEventPublisher eventPublisher;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;
    private final TransactionTemplate transactionTemplate;

    @PersistenceContext
    private EntityManager entityManager;

    private final Random random = new Random(20261115);

    private record Place(String contentId, String title, double lat, double lng) {
    }

    /** 시도 코드와 시군구 코드. 두 값을 이어 붙이지 않고 각각 둔다 */
    private record RegionKey(String regionCode, String code) {
    }

    private record Spot(Place festival, FatRegion region, Place hotel, List<Place> restaurants, List<Place> attractions) {
    }

    private record Visit(Place place, int startMinute, int endMinute, boolean scheduled) {
    }

    private record Trip(Spot spot, LocalDate startDate, LocalDate endDate, ItineraryStatus status) {
    }

    @Override
    public void run(String... args) {
        if (userRepository.existsByEmail(EMAIL_FORMAT.formatted(1))) {
            log.info("[DummyDataSeeder] 임시 데이터가 이미 있어 건너뜁니다.");
            return;
        }
        List<FatRegion> adoptable = fatService.regions();
        List<Spot> spots = spots(adoptable);
        if (spots.isEmpty()) {
            log.warn("[DummyDataSeeder] 펫을 키울 수 있는 시군구의 축제(tours)가 없어 건너뜁니다.");
            return;
        }
        int maxActiveFats = Optional.ofNullable(jdbcTemplate.queryForObject("SELECT max_active_fats FROM fat_settings LIMIT 1", Integer.class)).orElse(3);
        String passwordHash = passwordEncoder.encode(PASSWORD);
        LocalDateTime now = LocalDateTime.now(timelineProperties.zoneId());

        log.info("[DummyDataSeeder] 시작: 유저 {}명, 축제 {}곳", USER_COUNT, spots.size());
        for (int i = 1; i <= USER_COUNT; i++) {
            int sequence = i;
            boolean longTrip = i % 10 == 0;
            List<Trip> trips = trips(i, longTrip, spots, now.toLocalDate());
            Long userId = transactionTemplate.execute(status -> createUser(sequence, passwordHash, trips));
            List<Long> tripFatIds = transactionTemplate.execute(status -> adoptFats(sequence, userId, trips, adoptable, maxActiveFats, now));
            int points = 0;
            for (Trip trip : trips) {
                if (trip.status() != ItineraryStatus.READY) {
                    points += recordTimeline(userId, trip, now);
                }
            }
            if (longTrip) {
                graduate(userId, tripFatIds.getFirst());
            }
            log.info("[DummyDataSeeder] {}/{} userId={} 일정 {}개, 위치 기록 {}건", i, USER_COUNT, userId, trips.size(), points);
        }
        log.info("[DummyDataSeeder] 완료: 임시 유저 {}명 (이메일 {} ~ {}, 비밀번호 {})",
                USER_COUNT, EMAIL_FORMAT.formatted(1), EMAIL_FORMAT.formatted(USER_COUNT), PASSWORD);
    }

    /** 펫을 키울 수 있는 시군구마다 축제 하나씩, 최대 FESTIVAL_COUNT 곳 */
    private List<Spot> spots(List<FatRegion> adoptable) {
        Map<RegionKey, FatRegion> regions = adoptable.stream()
                .collect(Collectors.toMap(r -> new RegionKey(r.regionCode(), r.code()), r -> r, (a, b) -> a));
        Map<RegionKey, List<Place>> festivals = new LinkedHashMap<>();
        jdbcTemplate.query("""
                SELECT content_id, title, map_y, map_x, l_dong_regn_cd, l_dong_signgu_cd FROM tours
                WHERE content_type_id = '15' AND map_x IS NOT NULL AND map_y IS NOT NULL
                  AND l_dong_regn_cd <> '' AND l_dong_signgu_cd <> ''
                ORDER BY content_id
                """, rs -> {
            RegionKey key = new RegionKey(rs.getString("l_dong_regn_cd"), rs.getString("l_dong_signgu_cd"));
            if (regions.containsKey(key)) {
                festivals.computeIfAbsent(key, k -> new ArrayList<>())
                        .add(new Place(rs.getString("content_id"), rs.getString("title"), rs.getDouble("map_y"), rs.getDouble("map_x")));
            }
        });
        List<RegionKey> keys = new ArrayList<>(festivals.keySet());
        Collections.shuffle(keys, random);
        return keys.stream()
                .limit(FESTIVAL_COUNT)
                .map(key -> spot(festivals.get(key).get(random.nextInt(festivals.get(key).size())), regions.get(key), key))
                .toList();
    }

    private Spot spot(Place festival, FatRegion region, RegionKey key) {
        Map<String, List<Place>> nearby = new HashMap<>();
        jdbcTemplate.query("""
                SELECT content_id, title, map_y, map_x, content_type_id FROM tours
                WHERE l_dong_regn_cd = ? AND l_dong_signgu_cd = ? AND content_type_id IN ('32', '39', '12', '14')
                  AND map_x IS NOT NULL AND map_y IS NOT NULL
                """, rs -> {
            Place place = new Place(rs.getString("content_id"), rs.getString("title"), rs.getDouble("map_y"), rs.getDouble("map_x"));
            if (distanceKm(festival, place) <= NEARBY_KM) {
                String type = rs.getString("content_type_id").equals("32") ? "hotel"
                        : rs.getString("content_type_id").equals("39") ? "food" : "sight";
                nearby.computeIfAbsent(type, k -> new ArrayList<>()).add(place);
            }
        }, key.regionCode(), key.code());
        nearby.values().forEach(places -> places.sort(Comparator.comparingDouble(place -> distanceKm(festival, place))));
        Place hotel = pick(nearby.get("hotel"), 1, festival).getFirst();
        return new Spot(festival, region, hotel, pick(nearby.get("food"), 2, festival), pick(nearby.get("sight"), 2, festival));
    }

    private List<Place> pick(List<Place> places, int count, Place fallback) {
        List<Place> candidates = places == null ? new ArrayList<>() : new ArrayList<>(places.subList(0, Math.min(places.size(), count * 3)));
        Collections.shuffle(candidates, random);
        List<Place> picked = new ArrayList<>(candidates.subList(0, Math.min(candidates.size(), count)));
        while (picked.size() < count) {
            picked.add(fallback);
        }
        return picked;
    }

    /**
     * i 번째 유저의 일정. 첫 일정은 항상 지난 일정이고(10명마다 1명은 8일), 3명마다 1명은 진행 중 일정,
     * 4명마다 1명은 다가오는 일정이 더 있다. 일정끼리 기간이 겹치지 않는다.
     */
    private List<Trip> trips(int i, boolean longTrip, List<Spot> spots, LocalDate today) {
        List<Spot> shuffled = new ArrayList<>(spots);
        Collections.shuffle(shuffled, random);
        int nights = longTrip ? 7 : random.nextInt(3);
        LocalDate pastEnd = today.minusDays(2 + random.nextInt(longTrip ? 9 : 24));
        List<Trip> trips = new ArrayList<>();
        trips.add(new Trip(shuffled.get(0), pastEnd.minusDays(nights), pastEnd, ItineraryStatus.ENDED));
        if (!longTrip && i % 3 == 0) {
            trips.add(new Trip(shuffled.get(1 % shuffled.size()), today.minusDays(random.nextInt(2)), today.plusDays(1), ItineraryStatus.IN_PROGRESS));
        }
        if (i % 4 == 1) {
            LocalDate start = today.plusDays(5 + random.nextInt(25));
            trips.add(new Trip(shuffled.get(2 % shuffled.size()), start, start.plusDays(random.nextInt(3)), ItineraryStatus.READY));
        }
        return trips;
    }

    private Long createUser(int sequence, String passwordHash, List<Trip> trips) {
        User user = User.builder()
                .email(EMAIL_FORMAT.formatted(sequence))
                .password(passwordHash)
                .authType("BASIC")
                .role(UserRole.USER)
                .build();
        Profile profile = new Profile();
        profile.setUser(user);
        profile.setName(SURNAMES[random.nextInt(SURNAMES.length)] + GIVEN_NAMES[random.nextInt(GIVEN_NAMES.length)]);
        profile.setNickname(ADJECTIVES[random.nextInt(ADJECTIVES.length)] + NOUNS[random.nextInt(NOUNS.length)] + sequence);
        profile.setYear(1975 + random.nextInt(30));
        profile.setMonth(1 + random.nextInt(12));
        profile.setDay(1 + random.nextInt(28));
        user.setProfile(profile);
        Long userId = userRepository.save(user).getId();

        for (Trip trip : trips) {
            Itinerary itinerary = Itinerary.builder()
                    .startDate(trip.startDate())
                    .endDate(trip.endDate())
                    .userId(userId)
                    .status(trip.status())
                    .tour(entityManager.getReference(Tour.class, trip.spot().festival().contentId()))
                    .build();
            itinerary.setItineraryDays(days(itinerary, trip));
            Itinerary saved = itineraryRepository.save(itinerary);
            // 일정 서비스가 일정을 만들 때와 같이 일정 기간을 알린다 (펫 경험치의 일정 기간)
            eventPublisher.publishEvent(new ItineraryPeriodChangedEvent(saved.getId(), userId,
                    trip.spot().region().regionCode(), trip.spot().region().code(), trip.startDate(), trip.endDate()));
        }
        return userId;
    }

    private List<ItineraryDay> days(Itinerary itinerary, Trip trip) {
        List<ItineraryDay> days = new ArrayList<>();
        int dayNumber = 1;
        for (LocalDate date = trip.startDate(); !date.isAfter(trip.endDate()); date = date.plusDays(1), dayNumber++) {
            ItineraryDay day = ItineraryDay.builder()
                    .itinerary(itinerary)
                    .date(date)
                    .dayNumber(dayNumber)
                    .itinerarySchedules(new ArrayList<>())
                    .build();
            int sequence = 1;
            for (Visit visit : visits(trip, date)) {
                if (!visit.scheduled()) {
                    continue;
                }
                day.getItinerarySchedules().add(ItinerarySchedule.builder()
                        .itineraryDay(day)
                        .type("TOUR")
                        .tour(entityManager.getReference(Tour.class, visit.place().contentId()))
                        .title(visit.place().title())
                        .sequence(sequence++)
                        .startTime(LocalTime.MIN.plusMinutes(visit.startMinute()))
                        .endTime(visit.endMinute() >= 1440 ? LocalTime.of(23, 59) : LocalTime.MIN.plusMinutes(visit.endMinute()))
                        .build());
            }
            days.add(day);
        }
        return days;
    }

    /**
     * 하루 동선. 첫날은 10시에 도착하고 마지막 날은 축제를 보고 17시에 떠난다. 그 사이 밤은 숙소에서 보낸다.
     * 숙소에서 맞는 아침(0시~8시 30분)은 일정 항목으로 만들지 않는다.
     */
    private List<Visit> visits(Trip trip, LocalDate date) {
        Spot spot = trip.spot();
        boolean first = date.equals(trip.startDate());
        boolean last = date.equals(trip.endDate());
        List<Visit> visits = new ArrayList<>();
        if (!first) {
            visits.add(new Visit(spot.hotel(), 0, 510, false));
        }
        visits.add(new Visit(spot.attractions().get(0), first ? 600 : 540, 690, true));
        visits.add(new Visit(spot.restaurants().get(0), 720, 780, true));
        visits.add(new Visit(spot.festival(), 810, 1020, true));
        if (!last) {
            visits.add(new Visit(spot.attractions().get(1), 1050, 1110, true));
            visits.add(new Visit(spot.restaurants().get(1), 1140, 1200, true));
            visits.add(new Visit(spot.hotel(), 1230, 1440, true));
        }
        return visits;
    }

    /**
     * 일정 시군구마다 일정 시작 1~7일 전에 분양받은 펫을 만든다. 5명마다 1명은 아무 시군구에 미리 받은 펫이 하나 더 있다.
     *
     * @return 일정마다 그 시군구 펫 ID (최대 마리 수에 막혀 못 받았으면 null)
     */
    private List<Long> adoptFats(int sequence, Long userId, List<Trip> trips, List<FatRegion> adoptable, int maxActiveFats, LocalDateTime now) {
        Map<Long, Long> fatIdBySubdivision = new LinkedHashMap<>();
        List<Long> tripFatIds = new ArrayList<>();
        for (Trip trip : trips) {
            Long subdivisionId = trip.spot().region().regionSubdivisionId();
            if (!fatIdBySubdivision.containsKey(subdivisionId) && fatIdBySubdivision.size() < maxActiveFats) {
                LocalDateTime adoptedAt = trip.startDate().atStartOfDay()
                        .minusDays(1 + random.nextInt(7))
                        .plusHours(9 + random.nextInt(12))
                        .plusMinutes(random.nextInt(60));
                fatIdBySubdivision.put(subdivisionId, saveFat(userId, subdivisionId, adoptedAt.isBefore(now) ? adoptedAt : now.minusHours(1)));
            }
            tripFatIds.add(fatIdBySubdivision.get(subdivisionId));
        }
        if (sequence % 5 == 2 && fatIdBySubdivision.size() < maxActiveFats) {
            FatRegion extra = adoptable.get(random.nextInt(adoptable.size()));
            if (!fatIdBySubdivision.containsKey(extra.regionSubdivisionId())) {
                fatIdBySubdivision.put(extra.regionSubdivisionId(), saveFat(userId, extra.regionSubdivisionId(), now.minusDays(1 + random.nextInt(20))));
            }
        }
        return tripFatIds;
    }

    private Long saveFat(Long userId, Long subdivisionId, LocalDateTime adoptedAt) {
        return fatRepository.save(Fat.builder()
                .userId(userId)
                .regionSubdivisionId(subdivisionId)
                .adoptedAt(adoptedAt)
                .adoptionDaySeconds(0L)
                .build()).getId();
    }

    private int recordTimeline(Long userId, Trip trip, LocalDateTime now) {
        ZoneId zone = timelineProperties.zoneId();
        LocalDateTime latest = now.minusMinutes(1);
        int saved = 0;
        for (LocalDate date = trip.startDate(); !date.isAfter(trip.endDate()) && !date.isAfter(latest.toLocalDate()); date = date.plusDays(1)) {
            List<Visit> visits = visits(trip, date);
            List<TimelinePointsRequest.PointRequest> points = new ArrayList<>();
            for (int minute = visits.getFirst().startMinute(); minute < visits.getLast().endMinute(); minute++) {
                LocalDateTime at = date.atStartOfDay().plusMinutes(minute);
                if (at.isAfter(latest)) {
                    break;
                }
                double[] position = position(visits, minute);
                points.add(new TimelinePointsRequest.PointRequest(position[0], position[1], 5.0 + random.nextInt(20),
                        at.atZone(zone).toOffsetDateTime()));
            }
            if (!points.isEmpty()) {
                saved += timelineService.record(userId, new TimelinePointsRequest(points)).size();
            }
        }
        return saved;
    }

    /** 머무는 중이면 그곳에서 20m 안팎으로 흔들리고, 이동 중이면 두 곳 사이를 일정한 속도로 간다 */
    private double[] position(List<Visit> visits, int minute) {
        for (int k = 0; k < visits.size(); k++) {
            Visit visit = visits.get(k);
            if (minute >= visit.startMinute() && minute < visit.endMinute()) {
                return new double[]{
                        visit.place().lat() + 0.0002 * Math.sin(minute * 1.7),
                        visit.place().lng() + 0.0002 * Math.cos(minute * 1.3)};
            }
            Visit next = k + 1 < visits.size() ? visits.get(k + 1) : null;
            if (next != null && minute >= visit.endMinute() && minute < next.startMinute()) {
                double t = (minute - visit.endMinute() + 1.0) / (next.startMinute() - visit.endMinute() + 1.0);
                return new double[]{
                        visit.place().lat() + (next.place().lat() - visit.place().lat()) * t,
                        visit.place().lng() + (next.place().lng() - visit.place().lng()) * t};
            }
        }
        Place last = visits.getLast().place();
        return new double[]{last.lat(), last.lng()};
    }

    private void graduate(Long userId, Long fatId) {
        if (fatId == null) {
            return;
        }
        try {
            fatService.graduate(userId, fatId);
        } catch (IllegalArgumentException e) {
            log.info("[DummyDataSeeder] userId={} fatId={} 졸업하지 않음: {}", userId, fatId, e.getMessage());
        }
    }

    private static double distanceKm(Place a, Place b) {
        double dLat = Math.toRadians(b.lat() - a.lat());
        double dLng = Math.toRadians(b.lng() - a.lng());
        double h = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(a.lat())) * Math.cos(Math.toRadians(b.lat())) * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return 6371 * 2 * Math.asin(Math.sqrt(h));
    }
}
