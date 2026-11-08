package com.bosshi.maeul.ai;

/**
 * 다른 모듈이 AI 생성을 요청하는 계약. 제공자 선택, 순서, 장애 조치는 구현(ai 모듈)이 맡는다.
 */
public interface AiGenerator {

    /**
     * 제공자 순서대로 시도해 첫 성공 결과를 돌려준다.
     *
     * @throws com.bosshi.maeul.common.exception.ServiceUnavailableException 호출 가능한 제공자가 없을 때
     * @throws RuntimeException                                              시도한 제공자가 모두 실패했을 때 (마지막 실패)
     */
    <T> AiResult<T> generate(AiRequest<T> request);

    /**
     * 제외 대상이 아닌, 지금 호출 가능한 제공자가 하나라도 있는지
     */
    boolean hasAvailableProvider();
}
