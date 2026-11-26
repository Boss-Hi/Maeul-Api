package com.bosshi.maeul.territory.service;

import com.bosshi.maeul.gacha.GachaDrawer;
import com.bosshi.maeul.gacha.GachaType;
import com.bosshi.maeul.itineraries.openapi.entity.RegionSubdivisions;
import com.bosshi.maeul.itineraries.openapi.repository.LDongSigunguRepository;
import com.bosshi.maeul.territory.config.TerritoryProperties;
import com.bosshi.maeul.territory.dto.FatOverview;
import com.bosshi.maeul.territory.dto.FatRegion;
import com.bosshi.maeul.territory.dto.FatStatus;
import com.bosshi.maeul.territory.dto.GraduatedFat;
import com.bosshi.maeul.territory.dto.Graduation;
import com.bosshi.maeul.territory.entity.Fat;
import com.bosshi.maeul.territory.entity.FatLevel;
import com.bosshi.maeul.territory.entity.FatSetting;
import com.bosshi.maeul.territory.entity.TerritoryRegionCell;
import com.bosshi.maeul.territory.repository.FatItineraryPeriodRepository;
import com.bosshi.maeul.territory.repository.FatLevelRepository;
import com.bosshi.maeul.territory.repository.FatMissionExpRepository;
import com.bosshi.maeul.territory.repository.FatRepository;
import com.bosshi.maeul.territory.repository.FatSettingRepository;
import com.bosshi.maeul.territory.repository.TerritoryDailyStayRepository;
import com.bosshi.maeul.territory.repository.TerritoryRegionCellRepository;
import com.bosshi.maeul.territory.repository.dto.DateStay;
import com.bosshi.maeul.timeline.config.TimelineProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 펫: 전국 어느 시군구에서나 미리 분양받아 그 시군구에서 키운다 (ADR 0011).
 * 시군구는 region_subdivisions 의 한 행이다. 점령 시간과는 카카오가 위치 판정 때 준 시도·시군구 이름
 * (territory_region_cells)을 regions.name, region_subdivisions.name 과 맞춰 연결한다 (ADR 0012).
 * 경험치는 분양 이후이면서 그 시군구 일정 기간 안의 머문 시간(1분 = 1)과 방문일(ADR 0013), 미션 가챠로 얻은 값(ADR 0014)의 합이고,
 * 레벨은 지금 레벨표로 정한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FatService {

    private static final String ACTIVE_FAT_EXISTS = "이 시군구에는 이미 키우는 펫이 있습니다.";

    private final FatRepository fatRepository;
    private final FatLevelRepository levelRepository;
    private final FatSettingRepository settingRepository;
    private final TerritoryDailyStayRepository dailyStayRepository;
    private final TerritoryRegionCellRepository cellRepository;
    private final FatItineraryPeriodRepository periodRepository;
    private final FatMissionExpRepository missionExpRepository;
    private final LDongSigunguRepository sigunguRepository;
    private final GachaDrawer gachaDrawer;
    private final TerritoryProperties properties;
    private final TimelineProperties timelineProperties;

    /** 시도 이름과 시군구 이름 쌍. 카카오 판정 결과와 region_subdivisions 를 이 이름으로 맞춘다 */
    private record RegionNames(String sidoName, String sigunguName) {
        static RegionNames of(RegionSubdivisions subdivision) {
            return new RegionNames(subdivision.getRegion().getName(), subdivision.getName());
        }

        static RegionNames of(TerritoryRegionCell cell) {
            return new RegionNames(cell.getParentName(), cell.getRegionName());
        }
    }

    public FatOverview overview(Long userId) {
        FatLevelTable table = levelTable();
        FatSetting setting = setting();
        List<Fat> fats = fatRepository.findByUserIdAndGraduatedAtIsNullOrderByAdoptedAtAsc(userId);
        Map<Long, RegionSubdivisions> subdivisions = subdivisions(fats);
        return new FatOverview(
                fats.stream().map(fat -> status(fat, subdivisions.get(fat.getRegionSubdivisionId()), table, setting)).toList(),
                setting.getMaxActiveFats(),
                suggestedAdoptionRegions(userId, fats));
    }

    /** 졸업한 펫 (최근 졸업 순). 정렬은 고정이라 pageable 의 sort 는 쓰지 않는다 */
    public Page<GraduatedFat> graduated(Long userId, Pageable pageable) {
        Page<Fat> fats = fatRepository.findByUserIdAndGraduatedAtIsNotNullOrderByGraduatedAtDescIdDesc(
                userId, PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()));
        Map<Long, RegionSubdivisions> subdivisions = subdivisions(fats.getContent());
        return fats.map(fat -> graduated(fat, subdivisions.get(fat.getRegionSubdivisionId())));
    }

    /** 펫을 분양받을 수 있는 시군구: region_subdivisions 중 아래에 구가 있는 시를 뺀 곳 */
    public List<FatRegion> regions() {
        List<RegionSubdivisions> all = sigunguRepository.findAllWithRegion();
        Set<String> parents = all.stream()
                .flatMap(child -> all.stream()
                        .filter(parent -> parent.getRegion().getCode().equals(child.getRegion().getCode())
                                && child.getName().startsWith(parent.getName() + " "))
                        .map(parent -> parent.getRegion().getCode() + "/" + parent.getCode()))
                .collect(Collectors.toSet());
        return all.stream()
                .filter(subdivision -> !parents.contains(subdivision.getRegion().getCode() + "/" + subdivision.getCode()))
                .map(FatRegion::from)
                .sorted(Comparator.comparing(FatRegion::regionCode).thenComparing(FatRegion::code))
                .toList();
    }

    /**
     * 분양. region_subdivisions 의 region_code 와 code 를 각각 받는다.
     * 가 본 적이 없어도 전국 어느 시군구에서나, 키우는 펫이 없으면 받을 수 있다.
     * 키우는 펫이 최대 마리 수(fat_settings)에 이르렀으면 받을 수 없다.
     */
    @Transactional
    public FatStatus adopt(Long userId, String regionCode, String code) {
        RegionSubdivisions subdivision = sigunguRepository.findWithRegionByRegionCodeAndCode(regionCode, code)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 시군구입니다. regionCode: " + regionCode + ", code: " + code));
        if (sigunguRepository.hasDistrictsUnder(regionCode, subdivision.getName())) {
            throw new IllegalArgumentException(subdivision.getName() + "에서는 펫을 키울 수 없습니다. 아래 구에서 분양받아 주세요.");
        }
        if (fatRepository.existsByUserIdAndRegionSubdivisionIdAndGraduatedAtIsNull(userId, subdivision.getId())) {
            throw new IllegalArgumentException(ACTIVE_FAT_EXISTS);
        }
        FatSetting setting = setting();
        int maxActiveFats = setting.getMaxActiveFats();
        if (fatRepository.countByUserIdAndGraduatedAtIsNull(userId) >= maxActiveFats) {
            throw new IllegalArgumentException("펫은 동시에 " + maxActiveFats + "마리까지 키울 수 있습니다. 졸업하면 새로 분양받을 수 있습니다.");
        }

        Fat fat;
        try {
            fat = fatRepository.saveAndFlush(Fat.builder()
                    .userId(userId)
                    .regionSubdivisionId(subdivision.getId())
                    .adoptedAt(now())
                    .adoptionDaySeconds(0L)
                    .build());
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException(ACTIVE_FAT_EXISTS, e);
        }
        return status(fat, subdivision, levelTable(), setting);
    }

    /**
     * 졸업. 건국 레벨 이상인 펫만 할 수 있고, 어디서나 할 수 있으며 되돌리지 않는다.
     * 같은 트랜잭션에서 졸업 가챠를 뽑는다. 가챠를 뽑지 못하면 졸업도 하지 않는다 (ADR 0014).
     */
    @Transactional
    public Graduation graduate(Long userId, Long fatId) {
        Fat fat = fatRepository.findByIdAndUserId(fatId, userId)
                .orElseThrow(() -> new IllegalArgumentException("펫을 찾을 수 없습니다. ID: " + fatId));
        if (fat.isGraduated()) {
            throw new IllegalArgumentException("이미 졸업한 펫입니다.");
        }
        RegionSubdivisions subdivision = subdivision(fat.getRegionSubdivisionId());
        FatLevelTable.Progress progress = levelTable().progress(exp(fat, subdivision, setting()));
        if (!progress.founded()) {
            throw new IllegalArgumentException(progress.foundingLevel() == null
                    ? "레벨표에 건국 레벨이 없어 졸업할 수 없습니다."
                    : "건국(Lv." + progress.foundingLevel() + ")부터 졸업할 수 있습니다.");
        }
        fat.graduate(now(), progress.level(), progress.exp());
        return new Graduation(graduated(fat, subdivision), gachaDrawer.draw(userId, GachaType.GRADUATION, "fat:" + fat.getId()));
    }

    /**
     * 위치 기록이 바뀐 날짜에 분양받은 펫의 분양 시각 이후 점령 시간을 다시 계산한다.
     *
     * @param points 그날의 시간순 위치 기록 (시군구 판정 포함)
     */
    @Transactional
    public void recomputeAdoptionDay(Long userId, LocalDate date, List<OccupationCalculator.RegionPoint> points) {
        List<Fat> fats = fatRepository.findByUserIdAndGraduatedAtIsNullAndAdoptedAtGreaterThanEqualAndAdoptedAtLessThan(
                userId, date.atStartOfDay(), date.plusDays(1).atStartOfDay());
        Map<Long, RegionSubdivisions> subdivisions = subdivisions(fats);
        for (Fat fat : fats) {
            Set<String> occupationCodes = occupationCodes(subdivisions.get(fat.getRegionSubdivisionId()));
            List<OccupationCalculator.RegionPoint> afterAdoption = points.stream()
                    .filter(point -> !point.recordedAt().isBefore(fat.getAdoptedAt()))
                    .toList();
            fat.setAdoptionDaySeconds(OccupationCalculator.staySeconds(afterAdoption, properties.getMaxGap()).entrySet().stream()
                    .filter(entry -> occupationCodes.contains(entry.getKey()))
                    .mapToLong(Map.Entry::getValue)
                    .sum());
        }
    }

    /** 추천 분양 지역: 오늘 점령 시간이 있거나, 오늘 펫이 졸업했거나, 끝나지 않은 내 일정의 시군구 중 펫이 없는 곳 (분양 조건은 아님) */
    private List<FatRegion> suggestedAdoptionRegions(Long userId, List<Fat> activeFats) {
        LocalDate today = today();
        List<String> todayCodes = dailyStayRepository.findRegionIdsByUserAndDate(userId, today);
        Set<RegionNames> visited = todayCodes.isEmpty() ? Set.of() : cellRepository.findByRegionIdIn(new HashSet<>(todayCodes)).stream()
                .filter(cell -> cell.getRegionName() != null)
                .map(RegionNames::of)
                .collect(Collectors.toSet());
        Set<Long> graduatedToday = fatRepository.findByUserIdAndGraduatedAtIsNotNullOrderByGraduatedAtDesc(userId).stream()
                .filter(fat -> !fat.getGraduatedAt().isBefore(today.atStartOfDay()))
                .map(Fat::getRegionSubdivisionId)
                .collect(Collectors.toSet());
        Set<String> upcoming = periodRepository.findByUserIdAndEndDateGreaterThanEqual(userId, today).stream()
                .map(period -> period.getRegionCode() + "/" + period.getSubdivisionCode())
                .collect(Collectors.toSet());
        if (visited.isEmpty() && graduatedToday.isEmpty() && upcoming.isEmpty()) {
            return List.of();
        }
        Set<Long> raising = activeFats.stream().map(Fat::getRegionSubdivisionId).collect(Collectors.toSet());
        return sigunguRepository.findAllWithRegion().stream()
                .filter(subdivision -> visited.contains(RegionNames.of(subdivision)) || graduatedToday.contains(subdivision.getId())
                        || upcoming.contains(subdivision.getRegion().getCode() + "/" + subdivision.getCode()))
                .filter(subdivision -> !raising.contains(subdivision.getId()))
                .map(FatRegion::from)
                .sorted(Comparator.comparing(FatRegion::regionCode).thenComparing(FatRegion::code))
                .toList();
    }

    private FatStatus status(Fat fat, RegionSubdivisions subdivision, FatLevelTable table, FatSetting setting) {
        return new FatStatus(fat.getId(), FatRegion.from(subdivision), fat.getAdoptedAt(), table.progress(exp(fat, subdivision, setting)));
    }

    private GraduatedFat graduated(Fat fat, RegionSubdivisions subdivision) {
        return new GraduatedFat(fat.getId(), FatRegion.from(subdivision), fat.getAdoptedAt(),
                fat.getGraduatedAt(), fat.getGraduatedLevel(), fat.getGraduatedExp());
    }

    /** 분양 이후이면서 그 시군구 일정 기간 안의 머문 시간(분)과 방문일 경험치, 미션 가챠 경험치 */
    private long exp(Fat fat, RegionSubdivisions subdivision, FatSetting setting) {
        return occupationExp(fat, subdivision, setting) + missionExpRepository.sumExpByFatId(fat.getId());
    }

    private long occupationExp(Fat fat, RegionSubdivisions subdivision, FatSetting setting) {
        List<FatExpCalculator.Period> periods = periodRepository.findByUserIdAndRegionCodeAndSubdivisionCode(
                        fat.getUserId(), subdivision.getRegion().getCode(), subdivision.getCode()).stream()
                .map(period -> new FatExpCalculator.Period(period.getStartDate(), period.getEndDate()))
                .toList();
        if (periods.isEmpty()) {
            return 0;
        }
        LocalDate adoptionDate = fat.getAdoptedAt().toLocalDate();
        Set<String> codes = occupationCodes(subdivision);
        Map<LocalDate, Long> daily = codes.isEmpty() ? Map.of() : dailyStayRepository.sumByDateFrom(fat.getUserId(), codes, adoptionDate.plusDays(1)).stream()
                .collect(Collectors.toMap(DateStay::date, DateStay::seconds));
        return FatExpCalculator.exp(adoptionDate, fat.getAdoptionDaySeconds(), daily, periods, setting.getVisitDayExp());
    }

    /** 시군구(region_subdivisions 행)와 시도·시군구 이름이 같은 카카오 판정 결과의 점령 regionId */
    private Set<String> occupationCodes(RegionSubdivisions subdivision) {
        return new HashSet<>(cellRepository.findRegionIdsByNames(subdivision.getRegion().getName(), subdivision.getName()));
    }

    private RegionSubdivisions subdivision(Long id) {
        return subdivisions(List.of(id)).values().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("펫의 시군구(region_subdivisions)가 없습니다. ID: " + id));
    }

    private Map<Long, RegionSubdivisions> subdivisions(List<Fat> fats) {
        return subdivisions(fats.stream().map(Fat::getRegionSubdivisionId).collect(Collectors.toSet()));
    }

    private Map<Long, RegionSubdivisions> subdivisions(Collection<Long> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        return sigunguRepository.findWithRegionByIdIn(ids).stream()
                .collect(Collectors.toMap(RegionSubdivisions::getId, subdivision -> subdivision));
    }

    /** 펫별 지금 경험치 (키우는 중인 펫 기준으로 계산. 졸업한 펫은 graduatedExp 를 쓴다) */
    public Map<Long, Long> currentExps(List<Fat> fats) {
        if (fats.isEmpty()) {
            return Map.of();
        }
        FatSetting setting = setting();
        Map<Long, RegionSubdivisions> subdivisions = subdivisions(fats);
        return fats.stream().collect(Collectors.toMap(Fat::getId,
                fat -> exp(fat, subdivisions.get(fat.getRegionSubdivisionId()), setting)));
    }

    public FatSetting setting() {
        return settingRepository.findFirstByDeletedAtIsNullOrderByIdAsc()
                .orElseThrow(() -> new IllegalStateException("펫 설정(fat_settings)이 없습니다."));
    }

    public FatLevelTable levelTable() {
        List<FatLevel> levels = levelRepository.findByDeletedAtIsNullOrderByLevelAsc();
        return FatLevelTable.of(levels.stream()
                .map(level -> new FatLevelTable.Step(level.getLevel(), level.getRequiredExp(), level.getStageName(),
                        Boolean.TRUE.equals(level.getFounding())))
                .toList());
    }

    private LocalDate today() {
        return LocalDate.now(timelineProperties.zoneId());
    }

    private LocalDateTime now() {
        return LocalDateTime.now(timelineProperties.zoneId());
    }
}
