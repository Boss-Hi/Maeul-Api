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
- `GEMINI_MODEL`: 기본 `gemini-1.5-flash`
- `GEMINI_TEMPERATURE`: 기본 `0.7`
- `GEMINI_MAX_OUTPUT_TOKENS`: 기본 `512`
- `GEMINI_BATCH_ENABLED`: 기본 `false`
- `GEMINI_BATCH_PROMPT`: 배치 호출용 프롬프트
- `GEMINI_BATCH_CRON`: 기본 `0 0/30 * * * *`
- `GEMINI_RETRY_MAX_ATTEMPTS`: 일시적 오류(500/503/504) 시 모델당 최대 시도 횟수, 기본 `3`
- `GEMINI_RETRY_INITIAL_BACKOFF_MS`: 첫 재시도 대기(ms), 이후 2배씩 증가, 기본 `1000`
- `GEMINI_FALLBACK_MODEL`: 기본 모델 재시도 소진 시 사용할 대체 모델, 기본 없음

## Claude 환경 변수
- `ANTHROPIC_API_KEY`: `ai_api_keys` 에 CLAUDE 키가 없을 때 최초 기동 시 seed 되는 키
- `CLAUDE_MODEL`: 기본 `claude-opus-5-5`
- `CLAUDE_MAX_TOKENS`: 기본 `16000`
- `CLAUDE_EFFORT`: `low` | `medium` | `high` | `xhigh` | `max`, 기본 `medium`
- `CLAUDE_REFUSAL_FALLBACK_ENABLED`: 거절 시 서버 측 다른 Claude 모델로 재처리, 기본 `true`

## 일정 생성 제공자 순서
- `AI_PROVIDERS`: 시도 순서, 기본 `claude,gemini`. 로컬 무비용 테스트는 `test`

## 사용 예시
```java
GeminiGenerateResponse response = geminiService.generateText("서울 1일 여행 코스 추천해줘");
String text = response.getFirstText();
```

