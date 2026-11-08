package com.bosshi.maeul.itinerary.generator;

import com.bosshi.maeul.ai.service.ClaudeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Anthropic Claude 로 일정을 생성한다. (제공자 이름: claude)
 * structured outputs 로 {@link ClaudeItineraryOutput} 스키마를 강제하므로 별도 JSON 파싱이 없다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ClaudeItineraryGenerator extends AbstractAiItineraryGenerator {
    private final ClaudeService claudeService;

    /**
     * structured outputs 의 루트는 객체여야 하므로 일별 일정 배열을 days 로 감싼다.
     */
    public record ClaudeItineraryOutput(List<ItineraryDayJson> days) {
    }

    @Override
    public String providerName() {
        return "claude";
    }

    @Override
    public boolean isAvailable() {
        return claudeService.hasAvailableApiKey();
    }

    @Override
    protected List<ItineraryDayJson> requestDays(String prompt) {
        ClaudeItineraryOutput output = claudeService.generate(prompt, ClaudeItineraryOutput.class);
        log.info("Claude API 호출 성공: {}일", output.days() != null ? output.days().size() : 0);
        return output.days();
    }

    @Override
    protected String outputInstruction() {
        return """
                3. 일정은 days 배열에 날짜순으로 담으세요. date 는 YYYY-MM-DD 형식, sequence 는 그날 방문 순서(1부터)입니다.
                   추천 관광지를 쓴 항목은 contentId 만 채우고, 그 외 항목은 contentId 를 빈 문자열로 두고 title 과 description 을 채우세요.
                   모든 항목에 startTime, endTime 을 HH:mm 형식으로 채우세요.
                """;
    }
}
