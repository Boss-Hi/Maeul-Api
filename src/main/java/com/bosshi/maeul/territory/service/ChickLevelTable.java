package com.bosshi.maeul.territory.service;

import java.util.Comparator;
import java.util.List;

/**
 * 병아리 레벨표로 경험치에서 레벨을 정한다.
 * <p>
 * 가장 낮은 레벨에서 시작해 각 레벨의 requiredExp 를 채울 때마다 다음 레벨로 오른다. 가장 높은 레벨이 최대 레벨이며,
 * 최대 레벨에서는 경험치가 더 쌓여도 레벨이 오르지 않는다. founding 이 켜진 가장 낮은 레벨이 건국 레벨이다.
 */
public final class ChickLevelTable {

    public record Step(int level, long requiredExp, String stageName, boolean founding) {
    }

    /**
     * @param stageName      지금 레벨 이하에서 가장 가까운 단계 이름 (없으면 null)
     * @param levelExp       지금 레벨에서 쌓은 경험치
     * @param nextLevelExp   지금 레벨에서 다음 레벨까지 필요한 경험치 (최대 레벨이면 null)
     * @param foundingLevel  건국 레벨 (레벨표에 없으면 null)
     * @param founded        건국 레벨 이상이라 상품 교환이 가능한지
     */
    public record Progress(int level, String stageName, long exp, long levelExp, Long nextLevelExp,
                           boolean maxLevel, Integer foundingLevel, boolean founded) {
    }

    private final List<Step> steps;
    private final Integer foundingLevel;

    private ChickLevelTable(List<Step> steps) {
        this.steps = steps;
        this.foundingLevel = steps.stream().filter(Step::founding).map(Step::level).findFirst().orElse(null);
    }

    public static ChickLevelTable of(List<Step> steps) {
        if (steps.isEmpty()) {
            throw new IllegalStateException("병아리 레벨표가 비어 있습니다.");
        }
        return new ChickLevelTable(steps.stream().sorted(Comparator.comparingInt(Step::level)).toList());
    }

    public Progress progress(long exp) {
        long remaining = Math.max(exp, 0);
        int index = 0;
        String stageName = stageName(steps.getFirst(), null);
        while (index < steps.size() - 1 && remaining >= required(steps.get(index))) {
            remaining -= required(steps.get(index));
            index++;
            stageName = stageName(steps.get(index), stageName);
        }
        Step current = steps.get(index);
        boolean maxLevel = index == steps.size() - 1;
        return new Progress(current.level(), stageName, Math.max(exp, 0), remaining,
                maxLevel ? null : required(current), maxLevel, foundingLevel,
                foundingLevel != null && current.level() >= foundingLevel);
    }

    private static String stageName(Step step, String previous) {
        return step.stageName() == null || step.stageName().isBlank() ? previous : step.stageName();
    }

    private static long required(Step step) {
        return Math.max(step.requiredExp(), 0);
    }
}
