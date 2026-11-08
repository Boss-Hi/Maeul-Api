# AI Package (Gemini, Claude)

## 구성
- `config/GeminiProperties`: Gemini API 설정 바인딩
- `request/GeminiGenerateRequest`: `generateContent` 요청 DTO
- `response/GeminiGenerateResponse`: `generateContent` 응답 DTO
- `service/GeminiService`: Gemini API 호출 서비스. 거부된 키(API_KEY_INVALID, PERMISSION_DENIED)는 비활성화 후 다음 키로 재시도
- `service/AiApiKeyProvider`: 제공자별 키 선택/비활성화 (별도 트랜잭션)
- `service/ClaudeService`: Anthropic Claude 호출 (공식 Java SDK, structured outputs). 401/403 거부 키는 비활성화 후 다음 키로 재시도
- `config/ClaudeConfig`: Claude 설정 바인딩
- `batch/GeminiHealthBatch`: 주기적 헬스 체크 배치
- `json/GeminiJsonQuantifier`: JSON 구조 정량화 유틸

## 환경 변수
- `GEMINI_API_KEY`: 최초 기동 시 `ai_api_keys` 테이블이 비어 있으면 seed 되는 키. 런타임에는 DB 의 활성 키만 사용
- `GEMINI_BASE_URL`: 기본 `https://generativelanguage.googleapis.com`
- `GEMINI_MODEL`: 기본 `gemini-3.6-flash`
- `GEMINI_TEMPERATURE`: 기본 `0.5`
- `GEMINI_MAX_OUTPUT_TOKENS`: 기본 `4096`. 응답이 이 한도에 닿아 잘리면(`finishReason=MAX_TOKENS`) 오류로 처리하고 다음 제공자로 넘어간다
- `GEMINI_BATCH_ENABLED`: 기본 `false`
- `GEMINI_BATCH_PROMPT`: 배치 호출용 프롬프트
- `GEMINI_BATCH_CRON`: 기본 `0 0/30 * * * *`
- `GEMINI_RETRY_MAX_ATTEMPTS`: 일시적 오류(500/503/504) 시 모델당 최대 시도 횟수, 기본 `3`
- `GEMINI_RETRY_INITIAL_BACKOFF_MS`: 첫 재시도 대기(ms), 이후 2배씩 증가, 기본 `1000`
- `GEMINI_FALLBACK_MODEL`: 기본 모델 재시도 소진 시 사용할 대체 모델, 기본 없음

## Claude 환경 변수
- `ANTHROPIC_API_KEY`: `ai_api_keys` 에 CLAUDE 키가 없을 때 최초 기동 시 seed 되는 키
- `CLAUDE_MODEL`: 기본 `claude-sonnet-5-5`
- `CLAUDE_MAX_TOKENS`: 기본 `16000`
- `CLAUDE_EFFORT`: `low` | `medium` | `high` | `xhigh` | `max`, 기본 `medium`
- `CLAUDE_REFUSAL_FALLBACK_ENABLED`: 거절 시 서버 측 다른 Claude 모델로 재처리, 기본 `true`

## 제공자 순서
- `AI_PROVIDERS`: 시도 순서, 기본 `claude,gemini`. 앞 제공자가 실패하면 다음 제공자로 넘어간다
- 로컬 무비용 테스트는 `ITINERARY_GENERATOR=test` (일정 생성기 설정이며 `AI_PROVIDERS` 와 무관)

## 다른 모듈에서 쓰기
다른 모듈은 `GeminiService`/`ClaudeService` 를 쓰지 않고 루트 패키지의 `AiGenerator` 계약만 쓴다.
- `AiRequest(prompt, responseType, jsonExample, excludedProviders)`: `responseType` 은 스키마를 강제할 수 있는 제공자(Claude)에게, `jsonExample` 은 그렇지 못한 제공자(Gemini)에게 출력 형식으로 쓴다
- 받은 결과를 호출 측이 쓸 수 없으면 `request.excluding(result.provider())` 로 다른 제공자에게 다시 요청한다
- 호출 가능한 제공자가 없으면 `ServiceUnavailableException`, 시도한 제공자가 모두 실패하면 마지막 실패가 던져진다
- 제공자 과부하·요청 제한(429)·연결 실패는 제공자와 순서에 상관없이 `ServiceUnavailableException`(503)으로 통일된다. 거절(`ClaudeRefusalException`)과 파싱 실패 등은 그대로 던져진다(500)
- 서비스를 분리할 때는 `responseType`(`Class`)을 스키마로 바꿔야 한다

## 사용 예시
```java
AiResult<Answer> result = aiGenerator.generate(
        AiRequest.of("서울 1일 여행 코스 추천해줘", Answer.class, "{\"course\": [\"장소\"]}"));
Answer answer = result.value();
```

