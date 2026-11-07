package com.bosshi.maeul.timeline.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;

/**
 * 수집 주기에 맞춰 저장할 위치 기록을 고른다.
 * <p>
 * 이미 저장된 기록과 이번에 받은 기록을 통틀어, 서로 minimumGap 이상 떨어진 기록만 남긴다.
 * 받은 기록은 시간순으로 앞에서부터 채택하며, latestAllowed 이후 시각(미래)의 기록은 버린다.
 */
public final class TimelinePointSampler {

    private TimelinePointSampler() {
    }

    public static <T> List<T> sample(List<T> incoming,
                                     Function<T, LocalDateTime> recordedAt,
                                     Collection<LocalDateTime> stored,
                                     Duration minimumGap,
                                     LocalDateTime latestAllowed) {
        TreeSet<LocalDateTime> taken = new TreeSet<>(stored);
        List<T> sorted = incoming.stream()
                .filter(point -> !recordedAt.apply(point).isAfter(latestAllowed))
                .sorted(Comparator.comparing(recordedAt))
                .toList();

        List<T> accepted = new ArrayList<>();
        for (T point : sorted) {
            LocalDateTime time = recordedAt.apply(point);
            if (taken.contains(time)) {
                continue;
            }
            LocalDateTime before = taken.lower(time);
            LocalDateTime after = taken.higher(time);
            boolean farFromBefore = before == null || Duration.between(before, time).compareTo(minimumGap) >= 0;
            boolean farFromAfter = after == null || Duration.between(time, after).compareTo(minimumGap) >= 0;
            if (farFromBefore && farFromAfter) {
                taken.add(time);
                accepted.add(point);
            }
        }
        return accepted;
    }
}
