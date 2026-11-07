package com.bosshi.maeul.itinerary.generator;

import com.bosshi.maeul.common.exception.ServiceUnavailableException;
import com.bosshi.maeul.itinerary.dto.ItineraryGenerateDTO;
import com.bosshi.maeul.itinerary.entity.Itinerary;
import com.bosshi.maeul.itinerary.generator.impl.ItineraryGeneratorImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 제공자 순서(ai.providers)대로 일정 생성을 시도한다.
 * 앞 제공자가 어떤 이유로든 실패하면(키 없음, 재시도 소진, 거절, 파싱 실패 등) 다음 제공자로 넘어간다.
 */
@Slf4j
@Component
public class ItineraryGeneratorRouter {
    static final String UNAVAILABLE_MESSAGE = "일정 생성 서비스를 일시적으로 사용할 수 없습니다. 잠시 후 다시 시도해주세요.";

    private final List<ItineraryGeneratorImpl> orderedGenerators;

    public ItineraryGeneratorRouter(List<ItineraryGeneratorImpl> generators,
                                    @Value("${ai.providers:claude,gemini}") List<String> providerOrder) {
        Map<String, ItineraryGeneratorImpl> byName = generators.stream()
                .collect(Collectors.toMap(ItineraryGeneratorImpl::providerName, Function.identity()));

        List<ItineraryGeneratorImpl> ordered = new ArrayList<>();
        for (String name : providerOrder) {
            ItineraryGeneratorImpl generator = byName.get(name.trim().toLowerCase(Locale.ROOT));
            if (generator == null) {
                throw new IllegalStateException("알 수 없는 AI 제공자입니다: " + name + " (사용 가능: " + byName.keySet() + ")");
            }
            ordered.add(generator);
        }
        if (ordered.isEmpty()) {
            throw new IllegalStateException("ai.providers 에 AI 제공자를 1개 이상 지정해야 합니다.");
        }
        this.orderedGenerators = List.copyOf(ordered);
        log.info("일정 생성 AI 제공자 순서: {}", orderedGenerators.stream().map(ItineraryGeneratorImpl::providerName).toList());
    }

    /**
     * 제공자 순서 중 지금 호출 가능한 제공자가 하나라도 있는지
     */
    public boolean hasAvailableProvider() {
        return orderedGenerators.stream().anyMatch(ItineraryGeneratorImpl::isAvailable);
    }

    public Itinerary generate(ItineraryGenerateDTO dto) {
        RuntimeException lastFailure = null;

        for (ItineraryGeneratorImpl generator : orderedGenerators) {
            String provider = generator.providerName();
            if (!generator.isAvailable()) {
                log.warn("[{}] 사용 가능한 키가 없어 건너뜁니다.", provider);
                continue;
            }
            try {
                Itinerary itinerary = generator.generateItinerary(dto);
                log.info("[{}] 일정 생성 성공", provider);
                return itinerary;
            } catch (Exception e) {
                log.warn("[{}] 일정 생성 실패, 다음 제공자로 넘어갑니다: {}", provider, e.getMessage(), e);
                lastFailure = e instanceof RuntimeException re ? re : new IllegalStateException(e.getMessage(), e);
            }
        }

        if (lastFailure == null) {
            throw new ServiceUnavailableException(UNAVAILABLE_MESSAGE);
        }
        throw lastFailure;
    }
}
