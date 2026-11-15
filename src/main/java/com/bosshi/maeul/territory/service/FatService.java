package com.bosshi.maeul.territory.service;

import com.bosshi.maeul.territory.config.TerritoryProperties;
import com.bosshi.maeul.territory.dto.FatOverview;
import com.bosshi.maeul.territory.dto.FatOverview.AdoptableRegion;
import com.bosshi.maeul.territory.dto.FatStatus;
import com.bosshi.maeul.territory.dto.GraduatedFat;
import com.bosshi.maeul.territory.entity.Fat;
import com.bosshi.maeul.territory.entity.FatLevel;
import com.bosshi.maeul.territory.entity.TerritoryRegionCell;
import com.bosshi.maeul.territory.repository.FatLevelRepository;
import com.bosshi.maeul.territory.repository.FatRepository;
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
public class FatService {

    private static final String ACTIVE_FAT_EXISTS = "이 시군구에는 이미 키우는 병아리가 있습니다.";

    private final FatRepository fatRepository;
    private final FatLevelRepository levelRepository;
    private final TerritoryDailyStayRepository dailyStayRepository;
    private final TerritoryRegionCellRepository cellRepository;
    private final TerritoryProperties properties;
    private final TimelineProperties timelineProperties;

    public FatOverview overview(Long userId) {
        FatLevelTable table = levelTable();
        List<Fat> fats = fatRepository.findByUserIdAndGraduatedAtIsNullOrderByAdoptedAtAsc(userId);
        List<String> adoptable = adoptableRegionIds(userId, fats);
        Map<String, String> names = names(Stream.concat(fats.stream().map(Fat::getRegionId), adoptable.stream()).toList());

        return new FatOverview(
                fats.stream().map(fat -> status(fat, table, names)).toList(),
                adoptable.stream().map(regionId -> new AdoptableRegion(regionId, names.get(regionId))).toList());
    }

    public List<GraduatedFat> graduated(Long userId) {
        List<Fat> fats = fatRepository.findByUserIdAndGraduatedAtIsNotNullOrderByGraduatedAtDesc(userId);
        Map<String, String> names = names(fats.stream().map(Fat::getRegionId).toList());
        return fats.stream().map(fat -> graduated(fat, names)).toList();
    }

    /**
     * 분양. 오늘 점령 시간이 있는 시군구나 오늘 병아리가 졸업한 시군구 중, 키우는 병아리가 없는 곳에서 받을 수 있다.
     */
    @Transactional
    public FatStatus adopt(Long userId, String regionId) {
        if (fatRepository.existsByUserIdAndRegionIdAndGraduatedAtIsNull(userId, regionId)) {
            throw new IllegalArgumentException(ACTIVE_FAT_EXISTS);
        }
        LocalDate today = today();
        if (dailyStayRepository.sumByUserAndRegionBetween(userId, regionId, today, today.plusDays(1)) <= 0
                && !fatRepository.existsByUserIdAndRegionIdAndGraduatedAtGreaterThanEqual(userId, regionId, today.atStartOfDay())) {
            throw new IllegalArgumentException("오늘 점령 시간이 있는 시군구에서만 병아리를 분양받을 수 있습니다.");
        }

        Fat fat;
        try {
            fat = fatRepository.saveAndFlush(Fat.builder()
                    .userId(userId)
                    .regionId(regionId)
                    .adoptedAt(now())
                    .adoptionDaySeconds(0L)
                    .build());
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException(ACTIVE_FAT_EXISTS, e);
        }
        return status(fat, levelTable(), names(List.of(regionId)));
    }

    /**
     * 졸업 (상품 교환). 건국 레벨 이상인 병아리만 할 수 있고, 어디서나 할 수 있으며 되돌리지 않는다.
     */
    @Transactional
    public GraduatedFat graduate(Long userId, Long fatId) {
        Fat fat = fatRepository.findByIdAndUserId(fatId, userId)
                .orElseThrow(() -> new IllegalArgumentException("병아리를 찾을 수 없습니다. ID: " + fatId));
        if (fat.isGraduated()) {
            throw new IllegalArgumentException("이미 졸업한 병아리입니다.");
        }
        FatLevelTable.Progress progress = levelTable().progress(exp(fat));
        if (!progress.founded()) {
            throw new IllegalArgumentException(progress.foundingLevel() == null
                    ? "레벨표에 건국 레벨이 없어 졸업할 수 없습니다."
                    : "건국(Lv." + progress.foundingLevel() + ")부터 졸업할 수 있습니다.");
        }
        fat.graduate(now(), progress.level(), progress.exp());
        return graduated(fat, names(List.of(fat.getRegionId())));
    }

    /**
     * 위치 기록이 바뀐 날짜에 분양받은 병아리의 분양 시각 이후 점령 시간을 다시 계산한다.
     *
     * @param points 그날의 시간순 위치 기록 (시군구 판정 포함)
     */
    @Transactional
    public void recomputeAdoptionDay(Long userId, LocalDate date, List<OccupationCalculator.RegionPoint> points) {
        for (Fat fat : fatRepository.findByUserIdAndGraduatedAtIsNullAndAdoptedAtGreaterThanEqualAndAdoptedAtLessThan(
                userId, date.atStartOfDay(), date.plusDays(1).atStartOfDay())) {
            List<OccupationCalculator.RegionPoint> afterAdoption = points.stream()
                    .filter(point -> !point.recordedAt().isBefore(fat.getAdoptedAt()))
                    .toList();
            fat.setAdoptionDaySeconds(OccupationCalculator.staySeconds(afterAdoption, properties.getMaxGap())
                    .getOrDefault(fat.getRegionId(), 0L));
        }
    }

    private List<String> adoptableRegionIds(Long userId, List<Fat> activeFats) {
        LocalDate today = today();
        Set<String> raising = activeFats.stream().map(Fat::getRegionId).collect(Collectors.toSet());
        return Stream.concat(
                        dailyStayRepository.findRegionIdsByUserAndDate(userId, today).stream(),
                        fatRepository.findByUserIdAndGraduatedAtIsNotNullOrderByGraduatedAtDesc(userId).stream()
                                .filter(fat -> !fat.getGraduatedAt().isBefore(today.atStartOfDay()))
                                .map(Fat::getRegionId))
                .filter(regionId -> !raising.contains(regionId))
                .distinct()
                .sorted()
                .toList();
    }

    private FatStatus status(Fat fat, FatLevelTable table, Map<String, String> names) {
        return new FatStatus(fat.getId(), fat.getRegionId(), names.get(fat.getRegionId()), fat.getAdoptedAt(),
                table.progress(exp(fat)));
    }

    private GraduatedFat graduated(Fat fat, Map<String, String> names) {
        return new GraduatedFat(fat.getId(), fat.getRegionId(), names.get(fat.getRegionId()), fat.getAdoptedAt(),
                fat.getGraduatedAt(), fat.getGraduatedLevel(), fat.getGraduatedExp());
    }

    /** 분양 이후 그 시군구의 점령 시간(분) */
    private long exp(Fat fat) {
        LocalDate adoptionDate = fat.getAdoptedAt().toLocalDate();
        long seconds = fat.getAdoptionDaySeconds() + dailyStayRepository.sumByUserAndRegionFrom(
                fat.getUserId(), fat.getRegionId(), adoptionDate.plusDays(1));
        return seconds / 60;
    }

    private FatLevelTable levelTable() {
        List<FatLevel> levels = levelRepository.findByDeletedAtIsNullOrderByLevelAsc();
        return FatLevelTable.of(levels.stream()
                .map(level -> new FatLevelTable.Step(level.getLevel(), level.getRequiredExp(), level.getStageName(),
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
