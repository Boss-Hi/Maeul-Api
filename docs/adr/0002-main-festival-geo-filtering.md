# ADR 0002: MainFestival-Centered Geo-Filtering Architecture

## Status
**Accepted** (2026-08-26)

## Context

여행 일정 추천 시스템의 핵심 요구사항은 **사용자가 선택한 축제(MainFestival) 주변의 관광지만 추천**하는 것입니다. 이를 구현하기 위해서는 다음과 같은 설계 결정이 필요합니다:

### 문제 상황
1. 한국관광공사 API는 지역별 데이터 조회만 지원하고, 거리 기반 필터링 미지원
2. 축제 위치를 중심으로 반경 내의 관광지를 효율적으로 필터링해야 함
3. 여러 카테고리(축제, 숙박, 음식 등)에서 선택적으로 데이터 추출 필요

---

## Decision

**MainFestival을 여행의 지리적 중심점으로 설정하고, 반경 기반 필터링(Radius-based Filtering)**을 수행합니다.

### 구현 전략

#### 1. MainFestival 선택 프로세스
```
사용자 입력: 지역 + 카테고리
  ↓
한국관광공사 API에서 축제/공연(EV) 데이터 조회
  ↓
사용자가 MainFestival 선택 (축제명 클릭)
  ↓
축제의 위도/경도(mapx, mapy) 추출
```

#### 2. 반경 기반 필터링
- **기본 반경**: 10km (관리자 설정)
- **사용자 오버라이드**: 사용자가 5km ~ 20km 범위에서 조정 가능
- **거리 계산**: Haversine 공식을 사용한 구면 거리 계산
  ```java
  distance = 2 * R * asin(sqrt(sin²((lat2-lat1)/2) + cos(lat1)*cos(lat2)*sin²((lon2-lon1)/2)))
  // R = 지구 반지름 (6371 km)
  ```

#### 3. 다중 카테고리 필터링
- 한국관광공사 API에서 지역 내 모든 카테고리 데이터 조회
- 사용자가 선택한 카테고리만 필터링
- 각 카테고리별로 반경 필터링 적용

#### 4. 필터링 파이프라인
```
지역 + 카테고리 → API 조회 → 위도/경도 검증 → 반경 필터링 → 중복 제거 → AI 전달
```

---

## Consequences

### 긍정적 영향
- 사용자의 의도(MainFestival 중심)를 명확히 반영
- 직관적이고 이해하기 쉬운 설계
- 확장성이 좋음 (향후 다중 중심점 지원 가능)

### 부정적 영향
- API 호출 횟수 증가 (지역 내 모든 카테고리 조회 필요)
- 거리 계산으로 인한 계산 비용 증가
- MainFestival 위치가 정확하지 않으면 문제 발생

### 완화 방안
- Redis 캐싱으로 API 호출 횟수 감소 (ADR 0001 참조)
- 거리 계산 최적화 (벡터화, 사전 계산)
- 위도/경도 검증 로직 추가 (범위 체크)

---

## Filter Settings (필터 설정)

관리자가 다음 파라미터를 설정할 수 있습니다:

| 파라미터 | 기본값 | 범위 | 설명 |
|---------|-------|------|------|
| maxVenuesPerDay | 5 | 1~10 | 하루에 최대 추천 장소 수 |
| maxDistanceKm | 10 | 5~20 | MainFestival 주변 반경 |
| allowCategoryMixPerDay | true | true/false | 하루에 여러 카테고리 혼합 여부 |

사용자는 전역 설정을 오버라이드할 수 있습니다.

---

## Related ADRs
- ADR 0001: Real-time API Querying with Redis Caching
- ADR 0003: Gemini API를 통한 Itinerary 생성

---

## Implementation Notes

### 초기 구현 (Phase 1)
- 간단한 거리 계산 (Haversine)
- 데이터 정제 및 구조화
- 프로토타입 수준

### 향후 개선 (Phase 2)
- PostGIS를 활용한 고급 지역 쿼리
- 교통 시간 기반 필터링 (Google Maps API)
- 최적 경로 계산 (TSP 알고리즘)

---

## References
- Haversine 공식: https://en.wikipedia.org/wiki/Haversine_formula
- PostGIS: https://postgis.net/

