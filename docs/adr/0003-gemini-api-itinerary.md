# ADR 0003: Gemini API for Itinerary Generation with Cost Optimization

## Status
**Accepted** (2026-08-26)

## Context

여행 일정 추천 시스템의 핵심은 **AI(Google Gemini)가 필터링된 관광지 데이터를 기반으로 일별 추천 일정을 생성**하는 것입니다. 그러나 매 요청마다 AI 호출 시 비용이 발생하므로 최적화가 중요합니다.

### 고려사항
1. **비용 최적화**: Gemini API 호출 비용 절감 필수
2. **응답 품질**: 자연스럽고 실용적인 일정 생성
3. **데이터 정형화**: 일정을 구조화된 JSON으로 반환
4. **확장성**: 향후 다양한 AI 모델 교체 가능

---

## Decision

**Google Gemini API를 사용**하되, 다음의 비용 최적화 전략을 적용합니다:

### 1. Batch API 활용
- 실시간 요청 대신 배치 처리로 비용 50% 절감
- 사용 시나리오: 대량의 여행 일정 생성 요청

### 2. Prompt Caching
- 동일한 시스템 프롬프트와 맥락 정보를 캐싱
- 캐시 히트율: 약 90% (동일 지역, 유사한 기간)
- 캐시된 요청 비용: 90% 절감

### 3. 데이터 정형화 및 최소화
- 불필요한 정보 제거 (예: 상세 설명 제외)
- 필수 데이터만 전송: `title`, `category`, `location`, `distance`
- 평균 토큰 수: 500~800 (상세 정보 제외 시)

### 4. Prompt 구조
```
System Prompt:
"당신은 여행 일정 추천 전문가입니다. 제공된 관광지 목록과 여행 기간을 고려하여 
일별 대략적인 루트를 추천해주세요. 응답은 반드시 다음 JSON 형식을 따르세요:
{
  \"tripId\": \"...\",
  \"days\": [
    {
      \"date\": \"2026-09-01\",
      \"theme\": \"축제 중심\",
      \"venues\": [
        {\"name\": \"...\", \"category\": \"축제\", \"visitTime\": \"14:00-18:00\"},
        ...
      ]
    }
  ]
}"

User Prompt:
{
  "destination": "광주",
  "startDate": "2026-09-01",
  "endDate": "2026-09-10",
  "mainFestival": {
    "name": "광주 축제",
    "category": "축제/공연",
    "location": {lat: 35.1, lng: 126.8}
  },
  "selectedCategories": ["축제/공연", "체험관광/공예"],
  "filteredVenues": [
    {"name": "...", "category": "...", "distance": "2.5km", ...},
    ...
  ],
  "filterSettings": {
    "maxVenuesPerDay": 5,
    "allowCategoryMixPerDay": true
  }
}
```

---

## Implementation Strategy

### Phase 1: 기본 구현
- 실시간 API 호출
- 기본 프롬프트 사용
- 응답 JSON 파싱 및 검증

### Phase 2: 비용 최적화
- Prompt Caching 도입 (System Prompt 캐싱)
- 데이터 정형화 강화 (불필요 정보 제거)

### Phase 3: 배치 처리
- Batch API 도입 (야간 배치 실행)
- 사용자가 여행을 사전 예약하면 배치 작업으로 처리

---

## Consequences

### 긍정적 영향
- 고품질의 AI 기반 일정 추천
- 비용 최적화로 수익성 개선
- 정형화된 JSON 출력으로 클라이언트 파싱 용이
- 향후 다른 AI 모델(Claude, GPT 등)로 교체 가능

### 부정적 영향
- Gemini API 의존성 증가
- 프롬프트 엔지니어링 필요 (정확한 응답 유도)
- API 오류 시 대체 전략 필요
- 응답 시간이 AI 성능에 따라 변함 (평균 3~5초)

### 완화 방안
- 다양한 프롬프트 테스트 및 최적화
- Fallback 프롬프트 준비 (더 간단한 버전)
- 응답 JSON 검증 및 에러 처리 강화
- 타임아웃 설정 (10초)

---

## JSON Output Schema

```json
{
  "tripId": "string (UUID)",
  "destination": "string",
  "startDate": "string (YYYY-MM-DD)",
  "endDate": "string (YYYY-MM-DD)",
  "mainFestival": {
    "name": "string",
    "category": "string"
  },
  "days": [
    {
      "date": "string (YYYY-MM-DD)",
      "dayNumber": "integer",
      "theme": "string",
      "venues": [
        {
          "name": "string",
          "category": "string",
          "visitTime": "string (HH:MM-HH:MM)",
          "duration": "string (예: 2시간)",
          "description": "string (선택사항)"
        }
      ]
    }
  ],
  "summary": "string (여행 전체 요약)"
}
```

---

## Cost Estimation

### 초기 (Baseline)
- 평균 토큰: 2,000 (입력 1,200 + 출력 800)
- 요청당 비용: 약 $0.006
- 월간 1,000 요청 기준: $6

### Prompt Caching 적용
- 캐시 히트율 90%: 월간 900 요청
- 캐시된 요청: 약 $0.0006 / 요청
- 월간 비용: $0.60 + $5.40 = $6 → **$1.14 (81% 절감)**

### Batch API 적용
- Batch 가격: 50% 할인
- 월간 비용: **$0.57 (90% 절감)**

---

## Related ADRs
- ADR 0001: Real-time API Querying with Redis Caching
- ADR 0002: MainFestival-Centered Geo-Filtering

---

## Monitoring & Observability

```
메트릭 수집:
- API 호출 횟수
- 평균 응답 시간
- 에러율
- 캐시 히트율 (Phase 2 이후)
- 생성된 일정의 품질 점수 (사용자 피드백)
```

---

## References
- Google Gemini API: https://ai.google.dev/
- Prompt Caching: https://ai.google.dev/docs/caching
- Batch API: https://ai.google.dev/docs/batch

