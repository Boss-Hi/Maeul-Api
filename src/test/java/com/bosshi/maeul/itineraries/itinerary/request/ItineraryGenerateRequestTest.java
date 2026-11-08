package com.bosshi.maeul.itineraries.itinerary.request;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ItineraryGenerateRequest 하루 시간대 검증 테스트")
class ItineraryGenerateRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    @DisplayName("마감이 시작보다 늦지 않으면 거부한다")
    void rejectsEndNotAfterStart() {
        assertThat(validator.validate(request(LocalTime.of(20, 0), LocalTime.of(20, 0)))).hasSize(1);
        assertThat(validator.validate(request(LocalTime.of(21, 0), LocalTime.of(10, 0)))).hasSize(1);
    }

    @Test
    @DisplayName("한쪽만 주면 나머지는 기본값(09:30~22:30)과 비교한다")
    void comparesAgainstDefaultsWhenOnlyOneGiven() {
        assertThat(validator.validate(request(LocalTime.of(23, 0), null))).hasSize(1);
        assertThat(validator.validate(request(null, LocalTime.of(9, 0)))).hasSize(1);
        assertThat(validator.validate(request(LocalTime.of(8, 0), null))).isEmpty();
        assertThat(validator.validate(request(null, null))).isEmpty();
    }

    private static ItineraryGenerateRequest request(LocalTime start, LocalTime end) {
        return ItineraryGenerateRequest.builder().dayStartTime(start).dayEndTime(end).build();
    }
}
