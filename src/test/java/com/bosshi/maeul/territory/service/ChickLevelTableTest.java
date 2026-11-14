package com.bosshi.maeul.territory.service;

import com.bosshi.maeul.territory.service.ChickLevelTable.Progress;
import com.bosshi.maeul.territory.service.ChickLevelTable.Step;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ChickLevelTable 테스트")
class ChickLevelTableTest {

    private static final ChickLevelTable DEFAULT = ChickLevelTable.of(IntStream.rangeClosed(1, 100)
            .mapToObj(level -> new Step(level, level < 100 ? 126 : 0, level == 81 ? "건국" : null, level == 81))
            .toList());

    @Test
    @DisplayName("기본 레벨표는 10,080분(숙박 포함 7일)에 건국 Lv.81 이 된다")
    void foundingAtSevenDays() {
        assertThat(DEFAULT.progress(10_079)).isEqualTo(new Progress(80, null, 10_079, 125, 126L, false, 81, false));
        assertThat(DEFAULT.progress(10_080)).isEqualTo(new Progress(81, "건국", 10_080, 0, 126L, false, 81, true));
    }

    @Test
    @DisplayName("최대 레벨에서는 레벨이 멈추고 경험치는 계속 쌓인다")
    void maxLevel() {
        assertThat(DEFAULT.progress(12_474)).isEqualTo(new Progress(100, "건국", 12_474, 0, null, true, 81, true));
        assertThat(DEFAULT.progress(20_000)).isEqualTo(new Progress(100, "건국", 20_000, 7_526, null, true, 81, true));
    }

    @Test
    @DisplayName("레벨마다 필요 경험치가 다르면 각 레벨의 값을 차례로 채우고, 단계 이름은 가까운 아래 레벨의 이름을 쓴다")
    void perLevelRequirement() {
        ChickLevelTable table = ChickLevelTable.of(List.of(
                new Step(3, 0, "병아리", true), new Step(1, 10, "알", false), new Step(2, 20, null, false)));

        assertThat(table.progress(0)).isEqualTo(new Progress(1, "알", 0, 0, 10L, false, 3, false));
        assertThat(table.progress(29)).isEqualTo(new Progress(2, "알", 29, 19, 20L, false, 3, false));
        assertThat(table.progress(30)).isEqualTo(new Progress(3, "병아리", 30, 0, null, true, 3, true));
    }

    @Test
    @DisplayName("건국 레벨이 없으면 어떤 레벨에서도 교환할 수 없다")
    void noFounding() {
        ChickLevelTable table = ChickLevelTable.of(List.of(new Step(1, 5, null, false), new Step(2, 0, null, false)));

        assertThat(table.progress(100)).isEqualTo(new Progress(2, null, 100, 95, null, true, null, false));
    }
}
