package com.bosshi.maeul.territory.service;

import com.bosshi.maeul.territory.dto.MasterStatus;
import com.bosshi.maeul.territory.entity.Fat;
import com.bosshi.maeul.territory.entity.MasterLevel;
import com.bosshi.maeul.territory.repository.FatRepository;
import com.bosshi.maeul.territory.repository.MasterLevelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 주인: 펫을 키우는 사용자의 성장 기록. 사용자마다 1명이다.
 * 주인 경험치 = 모든 펫 경험치 합(키우는 중은 지금 값, 졸업은 졸업 때 값) x 주인 비율이고, 저장하지 않고 계산한다.
 * 레벨은 주인 레벨표로 정한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MasterService {

    private final FatRepository fatRepository;
    private final MasterLevelRepository levelRepository;
    private final FatService fatService;

    public MasterStatus master(Long userId) {
        long fatExpSum = fatExpSums(List.of(userId)).getOrDefault(userId, 0L);
        return new MasterStatus(fatExpSum, levelTable().progress(masterExp(fatExpSum, ratio())));
    }

    /** 사용자별 주인 경험치 */
    public Map<Long, Long> masterExps(Collection<Long> userIds) {
        BigDecimal ratio = ratio();
        Map<Long, Long> sums = fatExpSums(userIds);
        return userIds.stream().distinct()
                .collect(Collectors.toMap(userId -> userId, userId -> masterExp(sums.getOrDefault(userId, 0L), ratio)));
    }

    public FatLevelTable levelTable() {
        List<MasterLevel> levels = levelRepository.findByDeletedAtIsNullOrderByLevelAsc();
        return FatLevelTable.of(levels.stream()
                .map(level -> new FatLevelTable.Step(level.getLevel(), level.getRequiredExp(), level.getStageName(), false))
                .toList());
    }

    /** 사용자별 펫 경험치 합 (키우는 중은 지금 값, 졸업은 졸업 때 값) */
    private Map<Long, Long> fatExpSums(Collection<Long> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }
        List<Fat> fats = fatRepository.findByUserIdIn(new HashSet<>(userIds));
        Map<Long, Long> current = fatService.currentExps(fats.stream().filter(fat -> !fat.isGraduated()).toList());
        Map<Long, Long> sums = new HashMap<>();
        for (Fat fat : fats) {
            long exp = fat.isGraduated() ? Optional.ofNullable(fat.getGraduatedExp()).orElse(0L) : current.getOrDefault(fat.getId(), 0L);
            sums.merge(fat.getUserId(), exp, Long::sum);
        }
        return sums;
    }

    private BigDecimal ratio() {
        return fatService.setting().getMasterExpRatio();
    }

    private static long masterExp(long fatExpSum, BigDecimal ratio) {
        return BigDecimal.valueOf(fatExpSum).multiply(ratio).setScale(0, RoundingMode.DOWN).longValueExact();
    }
}
