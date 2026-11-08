package com.bosshi.maeul.ai.service;

import com.bosshi.maeul.ai.AiGenerator;
import com.bosshi.maeul.ai.AiRequest;
import com.bosshi.maeul.ai.AiResult;
import com.bosshi.maeul.ai.provider.AiProvider;
import com.bosshi.maeul.common.exception.ServiceUnavailableException;
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
 * 제공자 순서(ai.providers)대로 시도한다.
 * 앞 제공자가 어떤 이유로든 실패하면(키 없음, 재시도 소진, 거절, 파싱 실패 등) 다음 제공자로 넘어간다.
 */
@Slf4j
@Component
public class RoutingAiGenerator implements AiGenerator {
    static final String UNAVAILABLE_MESSAGE = "일정 생성 서비스를 일시적으로 사용할 수 없습니다. 잠시 후 다시 시도해주세요.";

    private final List<AiProvider> orderedProviders;

    public RoutingAiGenerator(List<AiProvider> providers,
                              @Value("${ai.providers:claude,gemini}") List<String> providerOrder) {
        Map<String, AiProvider> byName = providers.stream()
                .collect(Collectors.toMap(AiProvider::name, Function.identity()));

        List<AiProvider> ordered = new ArrayList<>();
        for (String name : providerOrder) {
            AiProvider provider = byName.get(name.trim().toLowerCase(Locale.ROOT));
            if (provider == null) {
                throw new IllegalStateException("알 수 없는 AI 제공자입니다: " + name + " (사용 가능: " + byName.keySet() + ")");
            }
            ordered.add(provider);
        }
        if (ordered.isEmpty()) {
            throw new IllegalStateException("ai.providers 에 AI 제공자를 1개 이상 지정해야 합니다.");
        }
        this.orderedProviders = List.copyOf(ordered);
        log.info("AI 제공자 순서: {}", orderedProviders.stream().map(AiProvider::name).toList());
    }

    @Override
    public boolean hasAvailableProvider() {
        return orderedProviders.stream().anyMatch(AiProvider::isAvailable);
    }

    @Override
    public <T> AiResult<T> generate(AiRequest<T> request) {
        RuntimeException lastFailure = null;

        for (AiProvider provider : orderedProviders) {
            String name = provider.name();
            if (request.excludedProviders().contains(name)) {
                continue;
            }
            if (!provider.isAvailable()) {
                log.warn("[{}] 사용 가능한 키가 없어 건너뜁니다.", name);
                continue;
            }
            try {
                T value = provider.generate(request);
                log.info("[{}] AI 생성 성공", name);
                return new AiResult<>(name, value);
            } catch (Exception e) {
                log.warn("[{}] AI 생성 실패, 다음 제공자로 넘어갑니다: {}", name, e.getMessage(), e);
                lastFailure = e instanceof RuntimeException re ? re : new IllegalStateException(e.getMessage(), e);
            }
        }

        if (lastFailure == null) {
            throw new ServiceUnavailableException(UNAVAILABLE_MESSAGE);
        }
        throw lastFailure;
    }
}
