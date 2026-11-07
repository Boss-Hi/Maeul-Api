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

### Survey (설문)
사용자 성향을 파악하기 위한 질문 묶음. 여러 개의 질문(SurveyQuestion)으로 구성됩니다.

- **질문(SurveyQuestion)**: 단일 선택(SINGLE_CHOICE) 또는 복수 선택(MULTIPLE_CHOICE). 필수 여부를 가짐
- **옵션(SurveyQuestionOption)**: 질문에 속한 선택지. 다른 질문의 옵션은 선택할 수 없음

### SurveyResponse (응답)
사용자 한 명이 설문 하나에 제출한 결과 전체. 여러 개의 답변(SurveyAnswer)을 가집니다.

- 응답을 수정하면 응답 자체는 유지되고, 그 안의 답변 전체가 새 답변으로 교체됩니다
- 응답은 제출한 본인만 수정할 수 있습니다

### SurveyAnswer (답변)
**답변 1개 = 선택한 옵션 1개.** 질문 하나에 대한 응답 전체가 아닙니다.

- 복수 선택 질문에서 옵션 2개를 고르면 답변이 2개 생깁니다
- 단일 선택 질문에는 답변이 정확히 1개입니다
- _Avoid_: "질문의 답변을 수정한다"는 표현. 답변은 개별 수정 대상이 아니며, 응답 단위로 교체됩니다

### Timeline (타임라인)
사용자 기기에서 수집한 위치 기록을 하루 단위로 묶어 보여주는 기록. Google 지도 타임라인과 같은 방식.

- **위치 기록(TimelinePoint)**: 기기가 수집 주기마다 보낸 좌표 1건. 본인만 저장·조회·삭제
- **수집 주기(Collection Interval)**: 위치 기록 사이의 간격 (기본 1분, 설정으로 변경). 더 촘촘한 기록은 저장하지 않음
- **머문 장소(STAY)**: 반경(기본 100m) 안에 최소 체류 시간(기본 5분) 이상 있었던 구간
- **이동(MOVE)**: 머문 장소 사이의 구간. 경로와 이동 거리를 가짐

### Territory (점령)
위치 기록으로 쌓은 시군구별 체류 시간. 오래 머문 사용자가 그 지역을 점령한 것으로 보고 월간 순위를 매긴다.

- **점령 시간(Occupation Time)**: 같은 시군구에 연속으로 있던 위치 기록 사이 시간의 합. 이동 중도 포함하고, 기록이 최대 간격(기본 3분)보다 오래 끊긴 시간은 뺀다
- **현재 방문(Current Visit)**: 마지막 위치 기록의 시군구에 연속으로 머문 구간. 마지막 기록이 최대 간격보다 오래되면 미확인(live=false)
- **개인 순서(Personal Order)**: 내 시군구들 사이의 누적 점령 시간 순서. 다른 사용자와의 순위가 아님
- **월간 순위(Monthly Rank)**: 한 시군구에서 한 달 동안 점령 시간이 있는 사용자 사이의 순위. 같은 시간은 같은 순위
- **포인트(Point)**: 전체 점령 시간이 단위(기본 1시간)를 넘을 때마다 적립(기본 100P). 위치 기록을 지워도 회수하지 않음
- 지역 ID: 전국 `KR`, 시도 2자리, 시군구 5자리(법정동 코드 앞자리)를 같은 체계로 쓴다

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
| 설문 | Survey | 질문(SurveyQuestion) 묶음 |
| 응답 | SurveyResponse | 사용자 한 명이 설문 하나에 제출한 결과 전체 |
| 답변 | SurveyAnswer | 선택한 옵션 1개 (질문 단위 아님) |
| AI 제공자 | AI Provider | 일정을 생성하는 외부 AI 서비스 (Gemini, Claude) |
| 제공자 순서 | Provider Order | 일정 생성 시 시도할 AI 제공자 순서. 앞 제공자가 실패하면 다음 제공자로 넘어감 |
| AI 키 | AiApiKey | 특정 AI 제공자에 속한 API 키. 같은 제공자의 키끼리만 돌아가며 사용 |
| 활성 키 | Active AiApiKey | 사용 가능한 AI 키. AI 제공자가 거부한 키는 자동으로 비활성이 되며, 사람이 교체·재활성화해야 함 |
| 타임라인 | Timeline | 위치 기록을 하루 단위 머문 장소·이동으로 묶은 기록 |
| 위치 기록 | TimelinePoint | 기기가 수집 주기마다 보낸 좌표 1건 |
| 수집 주기 | Collection Interval | 위치 기록 사이 간격 (기본 1분, 설정 가능) |
| 머문 장소 | STAY | 반경 안에 최소 체류 시간 이상 있었던 구간 |
| 이동 | MOVE | 머문 장소 사이의 구간 |
| 점령 | Territory | 위치 기록으로 쌓은 시군구별 체류 시간과 그 순위 |
| 점령 시간 | Occupation Time | 같은 시군구에 연속으로 있던 시간의 합 (긴 끊김 제외) |
| 현재 방문 | Current Visit | 마지막 기록의 시군구에 연속으로 머문 구간 |
| 개인 순서 | Personal Order | 내 시군구들 사이의 점령 시간 순서 |
| 월간 순위 | Monthly Rank | 한 시군구에서 한 달 동안의 사용자 간 순위 |
| 포인트 | Point | 점령 시간 단위마다 적립되는 보상 |
| 일정 생성 결과 | Itinerary Generate Result | 비동기 일정 생성의 성공(일정 ID) 또는 실패(사유) 알림 |

---

## 질문 및 토론 항목

- [ ] 향후 사용자 계정 시스템 도입 시 Trip과 Itinerary의 영속성 관리
- [ ] 나이대 기반 추천 로직 추가 시 Category 가중치 설정 방식
- [ ] Redis 캐싱 도입 시 캐시 유효기간 결정


