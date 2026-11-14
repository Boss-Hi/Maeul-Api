package com.bosshi.maeul.territory.service;

import com.bosshi.maeul.territory.config.TerritoryProperties;
import com.bosshi.maeul.territory.dto.ChickOverview;
import com.bosshi.maeul.territory.dto.ChickOverview.AdoptableRegion;
import com.bosshi.maeul.territory.dto.ChickStatus;
import com.bosshi.maeul.territory.dto.GraduatedChick;
import com.bosshi.maeul.territory.entity.Chick;
import com.bosshi.maeul.territory.entity.ChickLevel;
import com.bosshi.maeul.territory.entity.TerritoryRegionCell;
import com.bosshi.maeul.territory.repository.ChickLevelRepository;
import com.bosshi.maeul.territory.repository.ChickRepository;
import com.bosshi.maeul.territory.repository.TerritoryDailyStayRepository;
import com.bosshi.maeul.territory.repository.TerritoryRegionCellRepository;
import com.bosshi.maeul.timeline.config.TimelineProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 병아리: 사용자가 시군구 하나에서 점령 시간으로 키운다 (1분 = 경험치 1).
 * 경험치는 분양 이후 그 시군구의 점령 시간에서 계산하고, 레벨은 지금 레벨표로 정한다 (ADR 0010).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChickService {

    private static final String ACTIVE_CHICK_EXISTS = "이 시군구에는 이미 키우는 병아리가 있습니다.";

    private final ChickRepository chickRepository;
    private final ChickLevelRepository levelRepository;
    private final TerritoryDailyStayRepository dailyStayRepository;
    private final TerritoryRegionCellRepository cellRepository;
    private final TerritoryProperties properties;
    private final TimelineProperties timelineProperties;

    public ChickOverview overview(Long userId) {
        ChickLevelTable table = levelTable();
        List<Chick> chicks = chickRepository.findByUserIdAndGraduatedAtIsNullOrderByAdoptedAtAsc(userId);
        List<String> adoptable = adoptableRegionIds(userId, chicks);
        Map<String, String> names = names(Stream.concat(chicks.stream().map(Chick::getRegionId), adoptable.stream()).toList());

        return new ChickOverview(
                chicks.stream().map(chick -> status(chick, table, names)).toList(),
                adoptable.stream().map(regionId -> new AdoptableRegion(regionId, names.get(regionId))).toList());
    }

    public List<GraduatedChick> graduated(Long userId) {
        List<Chick> chicks = chickRepository.findByUserIdAndGraduatedAtIsNotNullOrderByGraduatedAtDesc(userId);
        Map<String, String> names = names(chicks.stream().map(Chick::getRegionId).toList());
        return chicks.stream().map(chick -> graduated(chick, names)).toList();
    }

    /**
     * 분양. 오늘 점령 시간이 있는 시군구나 오늘 병아리가 졸업한 시군구 중, 키우는 병아리가 없는 곳에서 받을 수 있다.
     */
    @Transactional
    public ChickStatus adopt(Long userId, String regionId) {
        if (chickRepository.existsByUserIdAndRegionIdAndGraduatedAtIsNull(userId, regionId)) {
            throw new IllegalArgumentException(ACTIVE_CHICK_EXISTS);
        }
        LocalDate today = today();
        if (dailyStayRepository.sumByUserAndRegionBetween(userId, regionId, today, today.plusDays(1)) <= 0
                && !chickRepository.existsByUserIdAndRegionIdAndGraduatedAtGreaterThanEqual(userId, regionId, today.atStartOfDay())) {
            throw new IllegalArgumentException("오늘 점령 시간이 있는 시군구에서만 병아리를 분양받을 수 있습니다.");
        }

        Chick chick;
        try {
            chick = chickRepository.saveAndFlush(Chick.builder()
                    .userId(userId)
                    .regionId(regionId)
                    .adoptedAt(now())
                    .adoptionDaySeconds(0L)
                    .build());
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException(ACTIVE_CHICK_EXISTS, e);
        }
        return status(chick, levelTable(), names(List.of(regionId)));
    }

    /**
     * 졸업 (상품 교환). 건국 레벨 이상인 병아리만 할 수 있고, 어디서나 할 수 있으며 되돌리지 않는다.
     */
    @Transactional
    public GraduatedChick graduate(Long userId, Long chickId) {
        Chick chick = chickRepository.findByIdAndUserId(chickId, userId)
                .orElseThrow(() -> new IllegalArgumentException("병아리를 찾을 수 없습니다. ID: " + chickId));
        if (chick.isGraduated()) {
            throw new IllegalArgumentException("이미 졸업한 병아리입니다.");
        }
        ChickLevelTable.Progress progress = levelTable().progress(exp(chick));
        if (!progress.founded()) {
            throw new IllegalArgumentException(progress.foundingLevel() == null
                    ? "레벨표에 건국 레벨이 없어 졸업할 수 없습니다."
                    : "건국(Lv." + progress.foundingLevel() + ")부터 졸업할 수 있습니다.");
        }
        chick.graduate(now(), progress.level(), progress.exp());
        return graduated(chick, names(List.of(chick.getRegionId())));
    }

    /**
     * 위치 기록이 바뀐 날짜에 분양받은 병아리의 분양 시각 이후 점령 시간을 다시 계산한다.
     *
     * @param points 그날의 시간순 위치 기록 (시군구 판정 포함)
     */
    @Transactional
    public void recomputeAdoptionDay(Long userId, LocalDate date, List<OccupationCalculator.RegionPoint> points) {
        for (Chick chick : chickRepository.findByUserIdAndGraduatedAtIsNullAndAdoptedAtGreaterThanEqualAndAdoptedAtLessThan(
                userId, date.atStartOfDay(), date.plusDays(1).atStartOfDay())) {
            List<OccupationCalculator.RegionPoint> afterAdoption = points.stream()
                    .filter(point -> !point.recordedAt().isBefore(chick.getAdoptedAt()))
                    .toList();
            chick.setAdoptionDaySeconds(OccupationCalculator.staySeconds(afterAdoption, properties.getMaxGap())
                    .getOrDefault(chick.getRegionId(), 0L));
        }
    }

    private List<String> adoptableRegionIds(Long userId, List<Chick> activeChicks) {
        LocalDate today = today();
        Set<String> raising = activeChicks.stream().map(Chick::getRegionId).collect(Collectors.toSet());
        return Stream.concat(
                        dailyStayRepository.findRegionIdsByUserAndDate(userId, today).stream(),
                        chickRepository.findByUserIdAndGraduatedAtIsNotNullOrderByGraduatedAtDesc(userId).stream()
                                .filter(chick -> !chick.getGraduatedAt().isBefore(today.atStartOfDay()))
                                .map(Chick::getRegionId))
                .filter(regionId -> !raising.contains(regionId))
                .distinct()
                .sorted()
                .toList();
    }

    private ChickStatus status(Chick chick, ChickLevelTable table, Map<String, String> names) {
        return new ChickStatus(chick.getId(), chick.getRegionId(), names.get(chick.getRegionId()), chick.getAdoptedAt(),
                table.progress(exp(chick)));
    }

    private GraduatedChick graduated(Chick chick, Map<String, String> names) {
        return new GraduatedChick(chick.getId(), chick.getRegionId(), names.get(chick.getRegionId()), chick.getAdoptedAt(),
                chick.getGraduatedAt(), chick.getGraduatedLevel(), chick.getGraduatedExp());
    }

    /** 분양 이후 그 시군구의 점령 시간(분) */
    private long exp(Chick chick) {
        LocalDate adoptionDate = chick.getAdoptedAt().toLocalDate();
        long seconds = chick.getAdoptionDaySeconds() + dailyStayRepository.sumByUserAndRegionFrom(
                chick.getUserId(), chick.getRegionId(), adoptionDate.plusDays(1));
        return seconds / 60;
    }

    private ChickLevelTable levelTable() {
        List<ChickLevel> levels = levelRepository.findByDeletedAtIsNullOrderByLevelAsc();
        return ChickLevelTable.of(levels.stream()
                .map(level -> new ChickLevelTable.Step(level.getLevel(), level.getRequiredExp(), level.getStageName(),
                        Boolean.TRUE.equals(level.getFounding())))
                .toList());
    }

    private Map<String, String> names(Collection<String> regionIds) {
        if (regionIds.isEmpty()) {
            return Map.of();
        }
        return cellRepository.findByRegionIdIn(new HashSet<>(regionIds)).stream()
                .filter(cell -> cell.getRegionName() != null)
                .collect(Collectors.toMap(TerritoryRegionCell::getRegionId, TerritoryRegionCell::getRegionName, (a, b) -> a));
    }

    private LocalDate today() {
        return LocalDate.now(timelineProperties.zoneId());
    }

    private LocalDateTime now() {
        return LocalDateTime.now(timelineProperties.zoneId());
    }
}
