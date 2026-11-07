package com.bosshi.maeul.timeline.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TimelinePointSampler 테스트")
class TimelinePointSamplerTest {

    private static final LocalDateTime BASE = LocalDateTime.of(2026, 11, 7, 10, 0);
    private static final Duration GAP = Duration.ofSeconds(55);
    private static final LocalDateTime NOW = BASE.plusHours(1);

    @Test
    @DisplayName("수집 주기보다 촘촘한 기록은 버리고 주기마다 하나씩 남긴다")
    void keepsOnePointPerInterval() {
        List<LocalDateTime> incoming = List.of(at(0), at(20), at(40), at(60), at(80), at(100), at(120));

        assertThat(sample(incoming, List.of())).containsExactly(at(0), at(60), at(120));
    }

    @Test
    @DisplayName("이미 저장된 기록과 가까운 기록은 앞뒤 모두 버린다")
    void respectsStoredNeighbours() {
        List<LocalDateTime> incoming = List.of(at(0), at(60), at(90));

        assertThat(sample(incoming, List.of(at(30)))).containsExactly(at(90));
    }

    @Test
    @DisplayName("이미 저장된 시각과 같은 기록은 간격이 0 이어도 버린다")
    void rejectsDuplicateTimestamp() {
        assertThat(TimelinePointSampler.sample(List.of(at(0), at(0)), Function.identity(), List.of(), Duration.ZERO, NOW))
                .containsExactly(at(0));
        assertThat(TimelinePointSampler.sample(List.of(at(0)), Function.identity(), List.of(at(0)), Duration.ZERO, NOW))
                .isEmpty();
    }

    @Test
    @DisplayName("허용 시각 이후(미래)의 기록은 버린다")
    void rejectsFuturePoints() {
        assertThat(sample(List.of(at(0), NOW.plusSeconds(1)), List.of())).containsExactly(at(0));
    }

    @Test
    @DisplayName("순서가 섞여 들어와도 시간순으로 고른다")
    void sortsBeforeSampling() {
        assertThat(sample(List.of(at(120), at(20), at(60), at(0)), List.of())).containsExactly(at(0), at(60), at(120));
    }

    private List<LocalDateTime> sample(List<LocalDateTime> incoming, List<LocalDateTime> stored) {
        return TimelinePointSampler.sample(incoming, Function.identity(), stored, GAP, NOW);
    }

    private static LocalDateTime at(int seconds) {
        return BASE.plusSeconds(seconds);
    }
}
