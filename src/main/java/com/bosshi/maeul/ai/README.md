# AI Gemini Package

## 구성
- `config/GeminiProperties`: Gemini API 설정 바인딩
- `request/GeminiGenerateRequest`: `generateContent` 요청 DTO
- `response/GeminiGenerateResponse`: `generateContent` 응답 DTO
- `service/GeminiService`: Gemini API 호출 서비스
- `batch/GeminiHealthBatch`: 주기적 헬스 체크 배치
- `json/GeminiJsonQuantifier`: JSON 구조 정량화 유틸

## 환경 변수
- `GEMINI_API_KEY`: Gemini API 키 (필수)
- `GEMINI_BASE_URL`: 기본 `https://generativelanguage.googleapis.com`
- `GEMINI_MODEL`: 기본 `gemini-1.5-flash`
- `GEMINI_TEMPERATURE`: 기본 `0.7`
- `GEMINI_MAX_OUTPUT_TOKENS`: 기본 `512`
- `GEMINI_BATCH_ENABLED`: 기본 `false`
- `GEMINI_BATCH_PROMPT`: 배치 호출용 프롬프트
- `GEMINI_BATCH_CRON`: 기본 `0 0/30 * * * *`

## 사용 예시
```java
GeminiGenerateResponse response = geminiService.generateText("서울 1일 여행 코스 추천해줘");
String text = response.getFirstText();
```

