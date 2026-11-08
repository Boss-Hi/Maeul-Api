package com.bosshi.maeul.survey.response;

import com.bosshi.maeul.survey.dto.PreferenceSummary;
import com.bosshi.maeul.survey.dto.PreferenceSummary.Choice;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PreferenceSummaryResponse JSON 형태 테스트")
class PreferenceSummaryResponseTest {

    private final JsonMapper mapper = JsonMapper.builder().build();

    @Test
    @DisplayName("프론트가 읽는 필드 이름과 값을 유지한다")
    void jsonShape() {
        JsonNode json = mapper.valueToTree(PreferenceSummaryResponse.from(new PreferenceSummary(
                new Choice("20대", "age_20"), new Choice("직장인", "worker"),
                List.of(new Choice("호텔", "hotel")), List.of(new Choice("커피", "coffee"), new Choice("웰니스", "wellness")))));

        assertThat(json.get("ageGroup").get("label").asString()).isEqualTo("20대");
        assertThat(json.get("ageGroup").get("value").asString()).isEqualTo("age_20");
        assertThat(json.get("occupation").get("value").asString()).isEqualTo("worker");
        assertThat(json.get("lodgings").get(0).get("label").asString()).isEqualTo("호텔");
        assertThat(json.get("themes")).hasSize(2);
    }

    @Test
    @DisplayName("답변이 없으면 null 과 빈 배열로 나간다")
    void emptyShape() {
        JsonNode json = mapper.valueToTree(PreferenceSummaryResponse.from(new PreferenceSummary(null, null, List.of(), List.of())));

        assertThat(json.get("ageGroup").isNull()).isTrue();
        assertThat(json.get("occupation").isNull()).isTrue();
        assertThat(json.get("lodgings").isArray()).isTrue();
        assertThat(json.get("themes")).isEmpty();
    }
}
