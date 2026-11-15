package com.bosshi.maeul.territory.service;

import com.bosshi.maeul.itineraries.openapi.entity.RegionSubdivisions;
import com.bosshi.maeul.itineraries.openapi.repository.LDongSigunguRepository;
import com.bosshi.maeul.territory.config.TerritoryProperties;
import com.bosshi.maeul.territory.dto.FatRanking;
import com.bosshi.maeul.territory.dto.MasterRanking;
import com.bosshi.maeul.territory.entity.Fat;
import com.bosshi.maeul.territory.repository.FatRepository;
import com.bosshi.maeul.user.entity.Profile;
import com.bosshi.maeul.user.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 지역별 펫·주인 순위. 지역은 시군구(regionCode + code), 시도(regionCode), 전국(둘 다 없음) 중 하나다.
 * 경험치를 조회할 때마다 계산하므로 지역의 펫이 많아지면 느려진다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FatRankingService {

    private static final String COUNTRY_NAME = "전국";

    private final FatRepository fatRepository;
    private final FatService fatService;
    private final MasterService masterService;
    private final LDongSigunguRepository sigunguRepository;
    private final ProfileRepository profileRepository;
    private final TerritoryProperties properties;

    /** 순위를 매길 지역. subdivisions 는 그 지역에 속한 시군구 */
    private record Scope(String regionCode, String code, String name, Map<Long, RegionSubdivisions> subdivisions) {
    }

    /** 키우는 중인 펫의 지금 경험치 순. 페이지 크기는 ranking-max-size 를 넘지 않고, pageable 의 sort 는 쓰지 않는다 */
    public FatRanking fatRanking(Long userId, String regionCode, String code, Pageable pageable) {
        Pageable page = PageRequest.of(pageable.getPageNumber(), limit(pageable.getPageSize()));
        Scope scope = scope(regionCode, code);
        List<Fat> fats = fatRepository.findByGraduatedAtIsNullAndRegionSubdivisionIdIn(scope.subdivisions().keySet());
        Map<Long, Long> exps = fatService.currentExps(fats);
        FatLevelTable table = fatService.levelTable();
        List<ScoreRanking.Ranked<Fat>> ranking = ScoreRanking.rank(fats, fat -> exps.get(fat.getId()), Comparator.comparing(Fat::getAdoptedAt));

        List<ScoreRanking.Ranked<Fat>> top = ranking.stream().skip(page.getOffset()).limit(page.getPageSize()).toList();
        Optional<ScoreRanking.Ranked<Fat>> mine = ranking.stream().filter(r -> r.item().getUserId().equals(userId)).findFirst();
        Map<Long, String> nicknames = nicknames(top, mine, r -> r.item().getUserId());
        java.util.function.Function<ScoreRanking.Ranked<Fat>, FatRanking.Entry> entry = r -> {
            RegionSubdivisions subdivision = scope.subdivisions().get(r.item().getRegionSubdivisionId());
            long exp = exps.get(r.item().getId());
            return new FatRanking.Entry(r.rank(), nicknames.get(r.item().getUserId()),
                    subdivision.getRegion().getName(), subdivision.getName(), table.progress(exp).level(), exp,
                    r.item().getUserId().equals(userId));
        };
        return new FatRanking(scope.regionCode(), scope.code(), scope.name(), ranking.size(),
                new PageImpl<>(top.stream().map(entry).toList(), page, ranking.size()), mine.map(entry).orElse(null));
    }

    /** 그 지역에서 펫을 키우는 중이거나 졸업시킨 사용자의 주인 경험치 전체 순. 페이지 크기는 ranking-max-size 를 넘지 않고, pageable 의 sort 는 쓰지 않는다 */
    public MasterRanking masterRanking(Long userId, String regionCode, String code, Pageable pageable) {
        Pageable page = PageRequest.of(pageable.getPageNumber(), limit(pageable.getPageSize()));
        Scope scope = scope(regionCode, code);
        Set<Long> userIds = fatRepository.findByRegionSubdivisionIdIn(scope.subdivisions().keySet()).stream()
                .map(Fat::getUserId)
                .collect(Collectors.toSet());
        Map<Long, Long> exps = masterService.masterExps(userIds);
        FatLevelTable table = masterService.levelTable();
        List<ScoreRanking.Ranked<Long>> ranking = ScoreRanking.rank(new ArrayList<>(userIds), exps::get, Comparator.naturalOrder());

        List<ScoreRanking.Ranked<Long>> top = ranking.stream().skip(page.getOffset()).limit(page.getPageSize()).toList();
        Optional<ScoreRanking.Ranked<Long>> mine = ranking.stream().filter(r -> r.item().equals(userId)).findFirst();
        Map<Long, String> nicknames = nicknames(top, mine, ScoreRanking.Ranked::item);
        java.util.function.Function<ScoreRanking.Ranked<Long>, MasterRanking.Entry> entry = r -> {
            long exp = exps.get(r.item());
            return new MasterRanking.Entry(r.rank(), nicknames.get(r.item()), table.progress(exp).level(), exp, r.item().equals(userId));
        };
        return new MasterRanking(scope.regionCode(), scope.code(), scope.name(), ranking.size(),
                new PageImpl<>(top.stream().map(entry).toList(), page, ranking.size()), mine.map(entry).orElse(null));
    }

    private Scope scope(String regionCode, String code) {
        boolean hasRegion = regionCode != null && !regionCode.isBlank();
        boolean hasCode = code != null && !code.isBlank();
        if (hasCode && !hasRegion) {
            throw new IllegalArgumentException("시군구 코드(code)는 시도 코드(regionCode)와 함께 보내야 합니다.");
        }
        Map<Long, RegionSubdivisions> subdivisions = sigunguRepository.findAllWithRegion().stream()
                .filter(s -> !hasRegion || s.getRegion().getCode().equals(regionCode))
                .filter(s -> !hasCode || s.getCode().equals(code))
                .collect(Collectors.toMap(RegionSubdivisions::getId, s -> s));
        if (subdivisions.isEmpty()) {
            throw new IllegalArgumentException("존재하지 않는 지역입니다. regionCode: " + regionCode + ", code: " + code);
        }
        RegionSubdivisions any = subdivisions.values().iterator().next();
        String name = !hasRegion ? COUNTRY_NAME
                : !hasCode ? any.getRegion().getName()
                : any.getRegion().getName() + " " + any.getName();
        return new Scope(hasRegion ? regionCode : null, hasCode ? code : null, name, subdivisions);
    }

    private int limit(int size) {
        return Math.clamp(size, 1, properties.getRankingMaxSize());
    }

    private <T> Map<Long, String> nicknames(List<T> top, Optional<T> mine, java.util.function.Function<T, Long> userOf) {
        Set<Long> userIds = new HashSet<>();
        top.forEach(r -> userIds.add(userOf.apply(r)));
        mine.ifPresent(r -> userIds.add(userOf.apply(r)));
        if (userIds.isEmpty()) {
            return Map.of();
        }
        return profileRepository.findByUserIdIn(userIds).stream()
                .collect(Collectors.toMap(p -> p.getUser().getId(), Profile::getNickname, (a, b) -> a));
    }
}
