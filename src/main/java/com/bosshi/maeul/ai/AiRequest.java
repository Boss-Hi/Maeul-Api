package com.bosshi.maeul.ai;

import java.util.HashSet;
import java.util.Set;

/**
 * AI 생성 요청.
 *
 * @param prompt            프롬프트 (제공자와 무관한 내용)
 * @param responseType      응답을 받을 타입. 스키마를 강제할 수 있는 제공자는 이 타입으로 강제한다
 * @param jsonExample       응답 JSON 예시. 스키마를 강제할 수 없는 제공자에게 출력 형식으로 보여준다 (없으면 null)
 * @param excludedProviders 시도하지 않을 제공자 이름. 받은 결과를 호출 측이 쓸 수 없을 때 다른 제공자로 다시 요청하는 데 쓴다
 */
public record AiRequest<T>(String prompt, Class<T> responseType, String jsonExample, Set<String> excludedProviders) {

    public AiRequest {
        excludedProviders = excludedProviders == null ? Set.of() : Set.copyOf(excludedProviders);
    }

    public static <T> AiRequest<T> of(String prompt, Class<T> responseType, String jsonExample) {
        return new AiRequest<>(prompt, responseType, jsonExample, Set.of());
    }

    /** 같은 요청에서 provider 를 시도 대상에서 뺀 요청 */
    public AiRequest<T> excluding(String provider) {
        Set<String> excluded = new HashSet<>(excludedProviders);
        excluded.add(provider);
        return new AiRequest<>(prompt, responseType, jsonExample, excluded);
    }
}
