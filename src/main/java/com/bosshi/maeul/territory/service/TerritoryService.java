package com.bosshi.maeul.territory.service;

import com.bosshi.maeul.openapi.repository.LDongRegionRepository;
import com.bosshi.maeul.point.service.PointService;
import com.bosshi.maeul.territory.config.TerritoryProperties;
import com.bosshi.maeul.territory.entity.TerritoryRegionCell;
import com.bosshi.maeul.territory.repository.TerritoryDailyStayRepository;
import com.bosshi.maeul.territory.repository.TerritoryRegionCellRepository;
import com.bosshi.maeul.territory.repository.dto.RegionStay;
import com.bosshi.maeul.territory.response.MyTerritoryResponse;
import com.bosshi.maeul.territory.response.MyTerritoryResponse.CurrentVisit;
import com.bosshi.maeul.territory.response.MyTerritoryResponse.RegionStat;
import com.bosshi.maeul.territory.response.TerritoryRankingResponse;
import com.bosshi.maeul.territory.response.TerritoryRegionResponse;
import com.bosshi.maeul.territory.type.TerritoryRegionType;
import com.bosshi.maeul.timeline.config.TimelineProperties;
import com.bosshi.maeul.timeline.repository.TimelinePointRepository;
import com.bosshi.maeul.user.entity.Profile;
import com.bosshi.maeul.user.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TerritoryService {

    private static final String COUNTRY_ID = "KR";
    /** 현재 방문을 찾을 때 거슬러 올라가는 최대 기간 */
    private static final java.time.Duration VISIT_LOOKBACK = java.time.Duration.ofDays(1);

    private final TerritoryDailyStayRepository dailyStayRepository;
    private final TerritoryRegionCellRepository cellRepository;
    private final TimelinePointRepository pointRepository;
    private final TerritoryPositionResolver positionResolver;
    private final TerritoryRewardService rewardService;
    private final PointService pointService;
    private final ProfileRepository profileRepository;
    private final LDongRegionRepository regionRepository;
    private final TerritoryProperties properties;
    private final TimelineProperties timelineProperties;

    public YearMonth currentMonth() {
        return YearMonth.now(timelineProperties.zoneId());
    }

    @Transactional
    public MyTerritoryResponse myTerritory(Long userId, YearMonth month) {
        Optional<OccupationCalculator.Visit> visit = currentVisit(userId);

        List<RegionStay> totals = dailyStayRepository.sumByRegion(userId).stream()
                .sorted(Comparator.comparing(RegionStay::seconds).reversed().thenComparing(RegionStay::regionId))
                .toList();
        Map<String, Long> monthly = dailyStayRepository
                .sumByRegionBetween(userId, month.atDay(1), month.plusMonths(1).atDay(1)).stream()
                .collect(Collectors.toMap(RegionStay::regionId, RegionStay::seconds));
        Map<String, TerritoryRegionCell> names = names(Stream.concat(
                totals.stream().map(RegionStay::regionId),
                visit.map(OccupationCalculator.Visit::regionId).stream()).toList());

        List<RegionStat> regions = new ArrayList<>();
        for (int i = 0; i < totals.size(); i++) {
            RegionStay total = totals.get(i);
            long monthlySeconds = monthly.getOrDefault(total.regionId(), 0L);
            List<TerritoryRanking.Ranked> ranking = monthlySeconds > 0 ? monthlyRanking(total.regionId(), month) : List.of();
            Integer monthlyRank = ranking.stream().filter(r -> r.userId().equals(userId)).map(TerritoryRanking.Ranked::rank).findFirst().orElse(null);
            TerritoryRegionCell name = names.get(total.regionId());
            regions.add(new RegionStat(
                    total.regionId(),
                    name == null ? null : name.getRegionName(),
                    name == null ? total.regionId().substring(0, 2) : name.getParentRegionId(),
                    name == null ? null : name.getParentName(),
                    total.seconds(), i + 1, monthlySeconds, monthlyRank, ranking.size()));
        }

        CurrentVisit currentVisit = visit.map(v -> {
            TerritoryRegionCell name = names.get(v.regionId());
            return new CurrentVisit(v.regionId(), name == null ? null : name.getRegionName(),
                    v.startedAt(), v.confirmedUntil(), v.confirmedStaySeconds(), v.live());
        }).orElse(null);

        return new MyTerritoryResponse(month, currentVisit, regions, rewardService.progress(userId),
                new MyTerritoryResponse.Points(pointService.balance(userId)));
    }

    public TerritoryRankingResponse ranking(Long userId, String regionId, YearMonth month, int size) {
        if (regionId == null || !regionId.matches("\\d{5}")) {
            throw new IllegalArgumentException("regionId 는 시군구 코드 5자리여야 합니다.");
        }
        int limit = Math.clamp(size, 1, properties.getRankingMaxSize());
        List<TerritoryRanking.Ranked> ranking = monthlyRanking(regionId, month);
        List<TerritoryRanking.Ranked> top = ranking.stream().limit(limit).toList();
        Optional<TerritoryRanking.Ranked> mine = ranking.stream().filter(r -> r.userId().equals(userId)).findFirst();

        Set<Long> userIds = new HashSet<>();
        top.forEach(r -> userIds.add(r.userId()));
        mine.ifPresent(r -> userIds.add(r.userId()));
        Map<Long, String> nicknames = profileRepository.findByUserIdIn(userIds).stream()
                .collect(Collectors.toMap(p -> p.getUser().getId(), Profile::getNickname, (a, b) -> a));
        Function<TerritoryRanking.Ranked, TerritoryRankingResponse.Entry> entry = r -> new TerritoryRankingResponse.Entry(
                r.rank(), nicknames.get(r.userId()), r.seconds(), r.userId().equals(userId));

        TerritoryRegionCell name = names(List.of(regionId)).get(regionId);
        return new TerritoryRankingResponse(regionId, name == null ? null : name.getRegionName(), month, ranking.size(),
                top.stream().map(entry).toList(), mine.map(entry).orElse(null));
    }

    /**
     * 지역 목록. parentRegionId 가 없으면 전체, 있으면 그 지역의 바로 아래 지역만.
     */
    public List<TerritoryRegionResponse> regions(String parentRegionId) {
        List<TerritoryRegionResponse> regions = new ArrayList<>();
        regions.add(new TerritoryRegionResponse(COUNTRY_ID, "전국", null, TerritoryRegionType.COUNTRY));
        regionRepository.findAllWithSigungus().stream()
                .sorted(Comparator.comparing(com.bosshi.maeul.openapi.entity.Region::getCode))
                .forEach(region -> {
                    regions.add(new TerritoryRegionResponse(region.getCode(), region.getName(), COUNTRY_ID, TerritoryRegionType.PROVINCE));
                    region.getSigungus().stream()
                            .sorted(Comparator.comparing(com.bosshi.maeul.openapi.entity.RegionSubdivisions::getCode))
                            .forEach(sigungu -> regions.add(new TerritoryRegionResponse(
                                    region.getCode() + sigungu.getCode(), sigungu.getName(), region.getCode(), TerritoryRegionType.DISTRICT)));
                });
        if (parentRegionId == null || parentRegionId.isBlank()) {
            return regions;
        }
        return regions.stream().filter(r -> parentRegionId.equals(r.parentRegionId())).toList();
    }

    private List<TerritoryRanking.Ranked> monthlyRanking(String regionId, YearMonth month) {
        return TerritoryRanking.rank(dailyStayRepository.sumByUserBetween(regionId, month.atDay(1), month.plusMonths(1).atDay(1)));
    }

    private Optional<OccupationCalculator.Visit> currentVisit(Long userId) {
        LocalDateTime now = LocalDateTime.now(timelineProperties.zoneId());
        List<OccupationCalculator.RegionPoint> points = positionResolver.resolve(
                pointRepository.findByUserIdAndRecordedAtGreaterThanEqualAndRecordedAtLessThanOrderByRecordedAtAsc(
                        userId, now.minus(VISIT_LOOKBACK), now.plusMinutes(2)));
        return OccupationCalculator.currentVisit(points, properties.getMaxGap(), now);
    }

    private Map<String, TerritoryRegionCell> names(Collection<String> regionIds) {
        if (regionIds.isEmpty()) {
            return Map.of();
        }
        return cellRepository.findByRegionIdIn(new HashSet<>(regionIds)).stream()
                .collect(Collectors.toMap(TerritoryRegionCell::getRegionId, Function.identity(), (a, b) -> a));
    }
}
