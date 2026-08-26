# ADR 0001: Real-time API Querying with Future Redis Caching Strategy

## Status
**Accepted** (2026-08-26)

## Context

여행 일정 추천 시스템은 한국관광공사 Open API에서 실시간으로 관광지 데이터를 조회해야 합니다. 초기 구현 단계에서는 빠른 프로토타입 개발이 중요하고, 나중에 성능 최적화를 진행합니다.

### 고려사항
1. **개발 속도**: 프로토타입 단계에서는 캐싱 로직보다 빠른 개발이 우선
2. **데이터 신선도**: 관광지 정보는 자주 변하지 않으므로 캐싱 가능
3. **비용**: Gemini API 호출이 주요 비용이므로 API 쿼리 최적화보다 덜 중요
4. **확장성**: 사용자 증가 시 API 응답 시간 문제 발생 가능

---

## Decision

**현재(Phase 1)**: 한국관광공사 API 실시간 조회  
**향후(Phase 2)**: Redis를 활용한 캐싱 전략 도입

### Phase 1 구현
- 사용자 요청 시마다 API 호출
- 비동기 처리로 응답 시간 개선
- 타임아웃 5초 + 1회 재시도

### Phase 2 계획
- Redis 캐시 도입
- 캐시 유효기간: 24시간 (일일 갱신)
- 캐시 키: `{region}:{category}` 형태
- 캐시 미스 시 실시간 API 호출 후 캐시 저장

---

## Consequences

### 긍정적 영향
- 초기 개발 속도 증가
- Redis 의존성 제거로 초기 복잡도 감소
- 데이터 신선도 보장

### 부정적 영향
- 동시 다중 사용자 시 API 호출 부하 증가
- 응답 시간이 API 성능에 직결
- 한국관광공사 API Rate Limit 초과 가능성

### 완화 방안
- Phase 2에서 Redis 캐싱 도입
- API 호출 횟수 제한 (사용자당 일일 제한)
- 배치 API 활용으로 비용 최적화

---

## Related ADRs
- ADR 0002: MainFestival 중심의 지역 필터링
- ADR 0003: Gemini API를 통한 Itinerary 생성

---

## References
- 한국관광공사 API 문서: https://api.visitkorea.or.kr/
- Redis 캐싱 패턴: https://redis.io/patterns/

