package com.bosshi.maeul.territory.service;

import com.bosshi.maeul.territory.config.TerritoryProperties;
import com.bosshi.maeul.territory.dto.FatOverview;
import com.bosshi.maeul.territory.dto.FatRegion;
import com.bosshi.maeul.territory.dto.FatStatus;
import com.bosshi.maeul.territory.dto.GraduatedFat;
import com.bosshi.maeul.territory.entity.Fat;
import com.bosshi.maeul.territory.entity.FatLevel;
import com.bosshi.maeul.territory.entity.FatSetting;
import com.bosshi.maeul.territory.entity.KakaoLegalDongCode;
import com.bosshi.maeul.territory.repository.FatLevelRepository;
import com.bosshi.maeul.territory.repository.FatRepository;
import com.bosshi.maeul.territory.repository.FatSettingRepository;
import com.bosshi.maeul.territory.repository.TerritoryDailyStayRepository;
import com.bosshi.maeul.territory.repository.KakaoLegalDongCodeRepository;
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
 * 병아리: 전국 어느 시군구에서나 미리 분양받아 그 시군구에서 키운다 (ADR 0011).
 * 시군구는 카카오 법정동 코드 표의 한 행이고, 그 코드가 점령의 regionId 와 같다 (ADR 0012).
 * 경험치는 지금 분양 이후 그 시군구의 점령 시간(1분 = 1)이고, 레벨은 지금 레벨표로 정한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FatService {

    private static final String ACTIVE_FAT_EXISTS = "이 시군구에는 이미 키우는 병아리가 있습니다.";

    private final FatRepository fatRepository;
    private final FatLevelRepository levelRepository;
    private final FatSettingRepository settingRepository;
    private final TerritoryDailyStayRepository dailyStayRepository;
    private final KakaoLegalDongCodeRepository legalDongCodeRepository;
    private final TerritoryProperties properties;
    private final TimelineProperties timelineProperties;

    public FatOverview overview(Long userId) {
        FatLevelTable table = levelTable();
        List<Fat> fats = fatRepository.findByUserIdAndGraduatedAtIsNullOrderByAdoptedAtAsc(userId);
        return new FatOverview(
                fats.stream().map(fat -> status(fat, table)).toList(),
                maxActiveFats(),
                suggestedAdoptionRegions(userId, fats));
    }

    public List<GraduatedFat> graduated(Long userId) {
        List<Fat> fats = fatRepository.findByUserIdAndGraduatedAtIsNotNullOrderByGraduatedAtDesc(userId);
        return fats.stream().map(this::graduated).toList();
    }

    /** 병아리를 분양받을 수 있는 시군구 전체 */
    public List<FatRegion> regions() {
        return legalDongCodeRepository.findByDeletedAtIsNullOrderByCodeAsc().stream().map(FatRegion::from).toList();
    }

    /**
     * 분양. 가 본 적이 없어도 전국 어느 시군구에서나, 키우는 병아리가 없으면 받을 수 있다.
     * 키우는 병아리가 최대 마리 수(fat_settings)에 이르렀으면 받을 수 없다.
     */
    @Transactional
    public FatStatus adopt(Long userId, Long legalDongCodeId) {
        KakaoLegalDongCode legalDongCode = legalDongCodeRepository.findById(legalDongCodeId)
                .filter(code -> !code.isDeleted())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 시군구입니다. legalDongCodeId: " + legalDongCodeId));
        if (fatRepository.existsByUserIdAndLegalDongCodeIdAndGraduatedAtIsNull(userId, legalDongCodeId)) {
            throw new IllegalArgumentException(ACTIVE_FAT_EXISTS);
        }
        int maxActiveFats = maxActiveFats();
        if (fatRepository.countByUserIdAndGraduatedAtIsNull(userId) >= maxActiveFats) {
            throw new IllegalArgumentException("병아리는 동시에 " + maxActiveFats + "마리까지 키울 수 있습니다. 졸업하면 새로 분양받을 수 있습니다.");
        }

        Fat fat;
        try {
            fat = fatRepository.saveAndFlush(Fat.builder()
                    .userId(userId)
                    .legalDongCode(legalDongCode)
                    .adoptedAt(now())
                    .adoptionDaySeconds(0L)
                    .build());
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException(ACTIVE_FAT_EXISTS, e);
        }
        return status(fat, levelTable());
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
        return graduated(fat);
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
                    .getOrDefault(fat.getLegalDongCode().getCode(), 0L));
        }
    }

    /** 추천 분양 지역: 오늘 점령 시간이 있거나 오늘 병아리가 졸업한 시군구 중 병아리가 없는 곳 (분양 조건은 아님) */
    private List<FatRegion> suggestedAdoptionRegions(Long userId, List<Fat> activeFats) {
        LocalDate today = today();
        Set<String> raising = activeFats.stream().map(fat -> fat.getLegalDongCode().getCode()).collect(Collectors.toSet());
        Set<String> codes = Stream.concat(
                        dailyStayRepository.findRegionIdsByUserAndDate(userId, today).stream(),
                        fatRepository.findByUserIdAndGraduatedAtIsNotNullOrderByGraduatedAtDesc(userId).stream()
                                .filter(fat -> !fat.getGraduatedAt().isBefore(today.atStartOfDay()))
                                .map(fat -> fat.getLegalDongCode().getCode()))
                .filter(code -> !raising.contains(code))
                .collect(Collectors.toSet());
        if (codes.isEmpty()) {
            return List.of();
        }
        return legalDongCodeRepository.findByCodeInAndDeletedAtIsNull(codes).stream()
                .sorted(Comparator.comparing(KakaoLegalDongCode::getCode))
                .map(FatRegion::from)
                .toList();
    }

    private FatStatus status(Fat fat, FatLevelTable table) {
        return new FatStatus(fat.getId(), FatRegion.from(fat.getLegalDongCode()), fat.getAdoptedAt(), table.progress(exp(fat)));
    }

    private GraduatedFat graduated(Fat fat) {
        return new GraduatedFat(fat.getId(), FatRegion.from(fat.getLegalDongCode()), fat.getAdoptedAt(),
                fat.getGraduatedAt(), fat.getGraduatedLevel(), fat.getGraduatedExp());
    }

    /** 분양 이후 그 시군구의 점령 시간(분) */
    private long exp(Fat fat) {
        LocalDate adoptionDate = fat.getAdoptedAt().toLocalDate();
        long seconds = fat.getAdoptionDaySeconds() + dailyStayRepository.sumByUserAndRegionFrom(
                fat.getUserId(), fat.getLegalDongCode().getCode(), adoptionDate.plusDays(1));
        return seconds / 60;
    }

    private int maxActiveFats() {
        return settingRepository.findFirstByDeletedAtIsNullOrderByIdAsc()
                .map(FatSetting::getMaxActiveFats)
                .orElseThrow(() -> new IllegalStateException("병아리 설정(fat_settings)이 없습니다."));
    }

    private FatLevelTable levelTable() {
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
