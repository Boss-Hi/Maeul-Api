# Maeul - Trip Recommendation System Context

## 프로젝트 개요

**Maeul**은 사용자의 프로필, 선호도, 일정을 기반으로 한국관광공사 Open API 데이터와 AI(Gemini)를 활용하여 맞춤형 여행 일정을 추천하는 시스템입니다.

---

## 핵심 도메인 개념

### User Profile (사용자 프로필)
사용자의 기본 정보로, 여행 추천의 입력값이 됩니다.

- **gender**: 성별 (man, girl)
- **birthDate**: 생년월일 (YYYY-MM-DD)
  - 현재는 사용하지 않으나 향후 나이대 기반 추천 확장 가능

### Trip (여행)
사용자가 계획하는 특정 지역, 기간의 여행.

- **destination**: 목적지 (광주, 부산, 대전 등)
- **startDate**: 여행 시작일
- **endDate**: 여행 종료일
- **selectedCategories**: 사용자가 선택한 관광 카테고리 (축제/공연, 체험관광/공예, 숙박/호텔, 음식 등)

### MainFestival (주축제)
여행의 중심이 되는 축제/공연. Trip의 지리적, 시간적 중심점 역할.

- **contentId**: 한국관광공사 API의 contentId
- **title**: 축제명
- **location**: 위치 (mapx, mapy - 위도/경도)
- **startDate**, **endDate**: 축제 개최 기간

### Category (카테고리)
사전 정의된 관광지 분류. 한국관광공사 API의 contenttypeid 매핑.

| 카테고리코드 | 카테고리명 | API contenttypeid 예시 |
|------------|---------|----------------------|
| AC | 숙박 | 32 |
| EV | 축제/공연 | 15 |
| EX | 체험관광/공예 | 12 |
| FD | 음식 | 39 |

### Venue (장소)
한국관광공사 API에서 조회한 개별 관광지/축제/식당/숙박.

- **contentId**: 고유 식별자
- **title**: 장소명
- **address**: 주소 (addr1, addr2)
- **location**: 위도/경도 (mapx, mapy)
- **category**: 카테고리
- **imageUrl**: 이미지 (firstimage)
- **description**: 설명 (tel, etc.)

### Itinerary (일정)
AI(Gemini)가 생성한 Trip에 대한 구체적인 추천 일정.

- **tripId**: 연결된 Trip의 ID
- **days**: 일별 일정 배열
  - **date**: 해당 날짜
  - **theme**: 해당 일의 테마 (예: "축제 중심", "체험 중심")
  - **venues**: 그 날 추천되는 Venue 리스트

### Filter Settings (필터 설정)
여행 일정 생성 시 적용되는 필터 조건.

- **maxVenuesPerDay**: 하루에 최대 추천 장소 수 (기본값: 5)
- **maxDistanceKm**: 축제 주변 반경 (기본값: 10km)
- **allowCategoryMixPerDay**: 하루에 여러 카테고리 혼합 여부 (기본값: true)
- **isGlobal**: 글로벌 설정 여부
- **userOverride**: 사용자 오버라이드 값

---

## 주요 흐름

### 여행 일정 생성 프로세스

1. **사용자 입력**: 기본 정보(gender, birthDate), 지역(city), 카테고리 선택
2. **축제 선택**: 선택한 지역의 축제 목록 조회 후 MainFestival 선택
3. **데이터 필터링**: 
   - 한국관광공사 API에서 실시간 데이터 조회
   - MainFestival 위치 기반 반경(maxDistanceKm) 내 Venue 필터링
   - 선택한 Category만 포함
4. **AI 일정 생성**:
   - 필터링된 Venue 리스트를 Gemini API에 전달
   - 여행 기간 동안의 Itinerary 생성
5. **결과 반환**: 정형화된 JSON 형식의 Itinerary 반환

---

## 기술적 특성

### API 통합
- **한국관광공사 Open API**: 실시간 조회 (향후 Redis 캐싱 계획)
- **Google Gemini API**: 일정 생성 (Prompt 캐싱, Batch API로 비용 최적화)

### 비동기 처리
- 한국관광공사 API 조회는 비동기로 실행
- 실패 시 재시도 로직 적용 (최대 1회)
- 타임아웃: 5초

### 데이터 정형화
- 한국관광공사 API 응답을 내부 Venue 모델로 변환
- Gemini 응답을 Itinerary JSON으로 정형화
- 비용 절약을 위해 최소 필수 데이터만 전송

---

## 현재 상태 (2026-08-26)

### MVP (Minimum Viable Product)
- 프로토타입 수준의 구현
- 기본 흐름만 동작 (데이터 구조 정하고 수정 후진 방식)
- 사용자 계정 기능 미포함

### 향후 확장
- Redis 캐싱
- 사용자 계정 및 여행 기록 저장
- 나이대 기반 추천 (생년월일 활용)
- 더 정교한 거리 계산 및 최적화 알고리즘

---

## 용어집 (Glossary)

| 용어 (KO) | 용어 (EN) | 설명 |
|----------|----------|------|
| 주축제 | MainFestival | 여행의 중심이 되는 축제/공연 |
| 장소 | Venue | 관광지/축제/식당/숙박 등 개별 관광 목적지 |
| 일정 | Itinerary | 생성된 여행 추천 일정 |
| 필터 설정 | Filter Settings | 일정 생성 시 적용되는 조건 |
| 카테고리 | Category | 관광지 분류 (축제, 숙박, 음식 등) |
| 사용자 선호 | User Preference | 사용자가 선택한 카테고리, 지역 등 |

---

## 질문 및 토론 항목

- [ ] 향후 사용자 계정 시스템 도입 시 Trip과 Itinerary의 영속성 관리
- [ ] 나이대 기반 추천 로직 추가 시 Category 가중치 설정 방식
- [ ] Redis 캐싱 도입 시 캐시 유효기간 결정


