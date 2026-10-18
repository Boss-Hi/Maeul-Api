# ADR 0005: Schedule Reorder — Full-Coverage Payload

## Status
**Accepted** (2026-10-18)

## Context

`ItinerarySchedule` 순서 변경 API(`PUT .../schedules/reorder`) 설계 시, 클라이언트가 변경할 항목만 보낼지(부분), 해당 day의 **모든** schedule을 빠짐없이 보낼지(전체) 결정이 필요했다.

## Decision

**클라이언트는 해당 day의 모든 ItinerarySchedule id를 페이로드에 포함해야 한다.** 누락된 id가 하나라도 있으면 `400 Bad Request`를 반환한다.

```json
PUT /api/itinerary/{itinerary}/days/{itineraryDay}/schedules/reorder

{
    "schedules": [
        {"id": 1, "sequence": 1},
        {"id": 3, "sequence": 2},
        {"id": 2, "sequence": 3}
    ]
}
```

## Rationale

- **부분 허용** 시, "지정하지 않은 schedule의 sequence는 그대로"라는 숨겨진 규칙이 생겨 클라이언트-서버 간 순서 상태가 diverge할 수 있다.
- 전체 명시를 강제하면 서버가 **항상 정답 상태를 단일 진실의 원천(source of truth)** 으로 유지할 수 있다.
- sequence 중복·누락 여부를 서버가 단번에 검증 가능하다.

## Consequences

### 긍정적 영향
- 순서 일관성 보장 — 서버와 클라이언트의 상태가 항상 동기화됨
- 검증 로직 단순화 — payload와 day의 schedule 집합을 set 비교로 처리
- 버그 추적 용이 — 의도하지 않은 부분 업데이트 없음

### 부정적 영향
- 클라이언트가 전체 id 목록을 미리 알아야 함 (show/list API 선행 호출 필요)
- schedule 수가 많을수록 payload 크기 증가

## Related ADRs
- ADR 0003: Gemini API for Itinerary Generation

