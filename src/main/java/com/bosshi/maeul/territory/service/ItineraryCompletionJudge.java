package com.bosshi.maeul.territory.service;

import com.bosshi.maeul.gacha.GachaDraw;
import com.bosshi.maeul.gacha.GachaDrawer;
import com.bosshi.maeul.gacha.GachaTicketIssuer;
import com.bosshi.maeul.gacha.GachaType;
import com.bosshi.maeul.gacha.TicketEventType;
import com.bosshi.maeul.itineraries.openapi.entity.RegionSubdivisions;
import com.bosshi.maeul.itineraries.openapi.repository.LDongSigunguRepository;
import com.bosshi.maeul.territory.config.TerritoryProperties;
import com.bosshi.maeul.territory.entity.FatItineraryPeriod;
import com.bosshi.maeul.territory.entity.ItineraryCompletion;
import com.bosshi.maeul.territory.repository.FatItineraryPeriodRepository;
import com.bosshi.maeul.territory.repository.ItineraryCompletionRepository;
import com.bosshi.maeul.territory.repository.TerritoryDailyStayRepository;
import com.bosshi.maeul.territory.repository.TerritoryRegionCellRepository;
import com.bosshi.maeul.timeline.config.TimelineProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 일정 완료 판정 (ADR 0014). 매일 종료일이 판정 기간 안인 일정을 본다.
 * 같은 사용자, 같은 주축제 시군구에 기간이 겹치는 일정들을 한 묶음으로 보고,
 * 묶음 기간 안에 그 시군구 점령 시간이 있는 날이 하루라도 있으면 완료하고 일정 완료 가챠를 1회 뽑는다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ItineraryCompletionJudge {

    private final FatItineraryPeriodRepository periodRepository;
    private final ItineraryCompletionRepository completionRepository;
    private final TerritoryDailyStayRepository dailyStayRepository;
    private final TerritoryRegionCellRepository cellRepository;
    private final LDongSigunguRepository sigunguRepository;
    private final GachaDrawer gachaDrawer;
    private final GachaTicketIssuer gachaTicketIssuer;
    private final TerritoryProperties properties;
    private final TimelineProperties timelineProperties;
    private final TransactionTemplate transactionTemplate;

    /** 같은 사용자, 같은 시군구에 기간이 겹치는 일정 묶음 */
    record Group(Long userId, String regionCode, String subdivisionCode, LocalDate startDate, LocalDate endDate, List<Long> itineraryIds) {
    }

    @Scheduled(cron = "${territory.itinerary-completion-cron:0 10 0 * * *}", zone = "${timeline.zone:Asia/Seoul}")
    public void judgeDaily() {
        judge(LocalDate.now(timelineProperties.zoneId()));
    }

    /** 종료일이 [today - 판정 기간, today - 1] 인 일정 묶음을 판정한다. 묶음마다 따로 커밋한다 */
    public void judge(LocalDate today) {
        LocalDate from = today.minusDays(properties.getItineraryCompletionWindow().toDays());
        LocalDate to = today.minusDays(1);
        Set<List<Object>> keys = periodRepository.findByEndDateBetween(from, to).stream()
                .map(period -> List.<Object>of(period.getUserId(), period.getRegionCode(), period.getSubdivisionCode()))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        for (List<Object> key : keys) {
            List<FatItineraryPeriod> periods = periodRepository.findByUserIdAndRegionCodeAndSubdivisionCode(
                    (Long) key.get(0), (String) key.get(1), (String) key.get(2));
            for (Group group : groups(periods)) {
                if (group.endDate().isBefore(from) || group.endDate().isAfter(to)) {
                    continue;
                }
                try {
                    transactionTemplate.executeWithoutResult(status -> complete(group));
                } catch (RuntimeException e) {
                    log.error("[ItineraryCompletionJudge] 일정 완료 판정 실패: userId={}, itineraryIds={}", group.userId(), group.itineraryIds(), e);
                }
            }
        }
    }

    /** 기간이 겹치는 일정끼리 묶는다 (모두 같은 사용자, 같은 시군구) */
    static List<Group> groups(List<FatItineraryPeriod> periods) {
        List<FatItineraryPeriod> sorted = periods.stream()
                .sorted(Comparator.comparing(FatItineraryPeriod::getStartDate).thenComparing(FatItineraryPeriod::getItineraryId))
                .toList();
        List<Group> groups = new ArrayList<>();
        Group current = null;
        for (FatItineraryPeriod period : sorted) {
            if (current != null && !period.getStartDate().isAfter(current.endDate())) {
                List<Long> ids = new ArrayList<>(current.itineraryIds());
                ids.add(period.getItineraryId());
                LocalDate end = period.getEndDate().isAfter(current.endDate()) ? period.getEndDate() : current.endDate();
                current = new Group(current.userId(), current.regionCode(), current.subdivisionCode(), current.startDate(), end, ids);
                groups.set(groups.size() - 1, current);
            } else {
                current = new Group(period.getUserId(), period.getRegionCode(), period.getSubdivisionCode(),
                        period.getStartDate(), period.getEndDate(), List.of(period.getItineraryId()));
                groups.add(current);
            }
        }
        return groups;
    }

    private void complete(Group group) {
        if (completionRepository.existsByItineraryIdIn(group.itineraryIds()) || !visited(group)) {
            return;
        }
        Long firstItineraryId = group.itineraryIds().stream().min(Long::compare).orElseThrow();
        GachaDraw draw = gachaDrawer.draw(group.userId(), GachaType.ITINERARY_COMPLETION, "itinerary:" + firstItineraryId);
        gachaTicketIssuer.issue(group.userId(), TicketEventType.ITINERARY_COMPLETION, "itinerary:" + firstItineraryId);
        completionRepository.saveAll(group.itineraryIds().stream()
                .map(itineraryId -> ItineraryCompletion.builder()
                        .userId(group.userId())
                        .itineraryId(itineraryId)
                        .gachaResultId(draw.resultId())
                        .build())
                .toList());
    }

    /** 묶음 기간 안에 그 시군구 점령 시간이 있는 날이 있는가. 시군구는 시도·시군구 이름으로 점령 regionId 와 맞춘다 (ADR 0012) */
    private boolean visited(Group group) {
        Optional<RegionSubdivisions> subdivision = sigunguRepository.findWithRegionByRegionCodeAndCode(group.regionCode(), group.subdivisionCode());
        if (subdivision.isEmpty()) {
            return false;
        }
        Set<String> codes = new HashSet<>(cellRepository.findRegionIdsByNames(subdivision.get().getRegion().getName(), subdivision.get().getName()));
        if (codes.isEmpty()) {
            return false;
        }
        return dailyStayRepository.sumByDateFrom(group.userId(), codes, group.startDate()).stream()
                .anyMatch(stay -> !stay.date().isAfter(group.endDate()) && stay.seconds() != null && stay.seconds() > 0);
    }
}
