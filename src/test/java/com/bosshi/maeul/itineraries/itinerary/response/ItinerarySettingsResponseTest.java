package com.bosshi.maeul.itineraries.itinerary.response;

import com.bosshi.maeul.itineraries.itinerary.dto.ItinerarySettings;
import com.bosshi.maeul.itineraries.itinerary.type.Pace;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ItinerarySettingsResponse JSON 형태 테스트")
class ItinerarySettingsResponseTest {

    @Test
    @DisplayName("프론트가 읽는 필드 이름, 페이스 값, HH:mm 시간 형식을 유지한다")
    void jsonShape() {
        JsonNode json = JsonMapper.builder().build().valueToTree(ItinerarySettingsResponse.from(
                new ItinerarySettings(Pace.MODERATE, LocalTime.of(9, 30), LocalTime.of(22, 30))));

        assertThat(json.get("paces")).hasSize(3);
        assertThat(json.get("paces").get(0).get("value").asString()).isEqualTo("RELAXED");
        assertThat(json.get("paces").get(0).get("title").asString()).isEqualTo("슬로우/힐링");
        assertThat(json.get("paces").get(0).get("description").asString()).isEqualTo("하루 2~3개 장소");
        assertThat(json.get("paces").get(1).get("value").asString()).isEqualTo("MODERATE");
        assertThat(json.get("paces").get(2).get("value").asString()).isEqualTo("PACKED");
        assertThat(json.get("defaultPace").asString()).isEqualTo("MODERATE");
        assertThat(json.get("dayStartTime").get("defaultValue").asString()).isEqualTo("09:30");
        assertThat(json.get("dayEndTime").get("defaultValue").asString()).isEqualTo("22:30");
        assertThat(json.get("dayStartTime").get("label").asString()).isEqualTo("일반적인 시작");
        assertThat(json.get("dayEndTime").get("label").asString()).isEqualTo("축제/야경 감상 후");
    }
}
