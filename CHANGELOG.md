# Changelog

이 프로젝트의 모든 주요 변경 사항은 이 문서에 기록됩니다.  
버전 규칙은 [Semantic Versioning](https://semver.org/lang/ko/)을 준수합니다.

---

## [Unreleased]

### In Progress

## [v0.1.4] - 2026-10-27

> 저장한 설문이 여러 개 일 때 1개만 조회되도록 수정

### Fixed
- 저장한 설문이 여러개 일 경우 1개만 조회

## [v0.1.3] - 2026-10-21

> kafka 연동 후 websocket (STOMP) 연동

### Added
- websocket (STOMP) 연동

### Fix
- kafka 연동 설정 변경
- jwt
  - 만료 시간 변경
  - 토근 정보 수정
- git action 수정

## [v0.1.2] - 2026-10-21

> kafka 연동 및 일정 생성 기능 kafka 이벤트 기반 비동기 처리 개발

### Added
- kafka 연동
- 일정 생성 기능 kafka 이벤트 기반 비동기 처리 개발

## [v0.1.1] - 2026-10-18

> 상세 일정 순서 변경 기능 개발

### Added
- ItinerarySchedule (일정 - 일별 일정 - 상세 일정) 순서 변경 API 추가

---

## [v0.1.0] - 2026-10-18

> 상세 일정 기능 개발

### Added
- ItineraryDay (일정 - 일별 일정) 조회 API 추가
- ItinerarySchedule (일정 - 일별 일정 - 상세 일정) 생성 API 추가
- ItinerarySchedule (일정 - 일별 일정 - 상세 일정) 조회 API 추가
- ItinerarySchedule (일정 - 일별 일정 - 상세 일정) 수정 API 추가
- 매핑 용 주소 데이터 추가

### Fixed
- 로그인 오류 시 500 Server Error 대신 401 Unauthorized 응답 반환 수정
- 일정 response 중 tour 네이밍을 schedule 로 변경

---

## [v0.0.3] - 2026-10-15

> 소셜 로그인 완성, 마이페이지·프로필 관리, 설문·미션 API 고도화

### Added
- 카카오(Kakao) OAuth2 소셜 로그인 분리 및 멀티 프로바이더 지원 완성 (`bda5733`)
- 가입 유형(소셜 / 일반) 구분 필드 추가 (`ded75b4`)
- 프로필 조회 · 수정 API 추가 (`f81bb90`, `30bd926`)
- 프로필 이미지 업로드 기능 (`3c36804`)
- 저장된 설문 응답 조회 API 추가 (`7140c59`)
- 설문 응답 수정 API 추가 (`bb0fe49`)
- 현재 진행 중 미션 조회 API 수정 (`dd9f3f4`)
- 일정 조회 응답(Response) 구조 개선 (`92d2a54`)
- 축제 인기(Popular) 정렬 기능 추가 (`8e41077`)
- Swagger(OpenAPI) 문서 자동화 설정 추가

### Fixed
- JWT 토큰 만료 시 500 Server Error 대신 401 Unauthorized 응답 반환 수정
- OAuth2 인증 오류 처리 개선 (`a39bbbe`)
- Flyway 마이그레이션 스크립트 수정 (`5469bfd`, `bdf57d9`)
- BaseEntity 공통 필드 기본값 수정 (`3558bb8`)

### Security
- Spring Security 패치 포함 버전으로 의존성 업데이트

---

## [v0.0.2] - 2026-09-21

> 핵심 도메인 완성: 일정·미션·축제·어드민·AI 연동, 인프라 고도화

### Added
- **일정(Itinerary)**: AI 기반 여행 일정 생성 및 조회 API (`c92bf77`)
- **미션(Mission)**: 미션 도메인 추가 및 일정-미션 연동 (`4b4f73c`, `4c78fbe`)
- **미션 시스템**: 미션 DB 스키마 설계, MissionSeeder 추가 (`4d6a94c`, `da6cb47`)
- **미션 구조 개편**: 미션 로직 및 구조 전면 수정 (`e2c939f`)
- **축제(Festival)**: 축제 카테고리 및 목록 조회 API 수정 (`d337865`)
- **축제 검색**: 키워드 기반 축제 검색 API 구현 (`fbc2f6f`)
- **지역 조회**: 시·도 및 시·군·구 코드 기반 지역 조회 API 추가 (`dafac5b`)
- **설문(Survey)**: 설문 조회 및 등록 API 추가 (`2c1e5ac`)
- **AI 통합**: Google Gemini API 일정 생성 연동 (`3b113ec`, `81e09eb`)
- **위치 정보**: 시·군·구 좌표 데이터 추가 (`a6e284c`, `1e29028`, `e805c7d`)
- **페이지네이션**: 목록 API 페이지네이션 지원 (`82b0c72`)
- **어드민(Admin)**: 어드민 계정 및 로그인 분리 구현 (`54ca3b2`, `1dc199a`)
- **어드민 접근 제어**: IP 기반 도메인 필터 설정 (`8153372`, `b93af80`)
- **어드민 테이블**: Admin 관련 DB 마이그레이션 추가 (`bef73ca`)
- **Redis**: 세션 및 캐시용 Redis 설정 추가 (`5012f74`)
- **AES 암호화**: 개인정보 컬럼 암호화 적용 (`88afc97`, `73dced7`)
- **AI API Key 암호화**: Gemini API 키 보안 처리 (`90bbff1`)
- **카카오 로그인**: Kakao OAuth2 Provider 초기 연동 (`ae90f1b`)
- **OAuth2 Redirect**: 소셜 로그인 리다이렉트 설정 완성 (`99807c2`)
- **TourSeeder**: 관광지 초기 데이터 Seeder 추가 (`c337512`)

### Changed
- Dockerfile.prod 및 CI/CD 배포 파이프라인 개선 (`98a4317`, `2c38e5e` 등)
- Entity 공통 필드 및 기본값 정비 (`ba6ef7a`, `97e7cc2`)
- GlobalExceptionHandler 에러 응답 표준화 (`ba35b90`)
- docker-compose.prod.yml 서비스 구성 업데이트 (`d3b1f68`)

---

## [v0.0.1] - 2026-08-18

> 프로젝트 초기화 및 기반 인프라 구성

### Added
- Java 26 + Spring Boot 4.1.0 기반 프로젝트 초기화 (`762a15a`)
- Gradle 빌드 설정 및 MSA 기반 패키지 구조 구성 (`8bec4ca`)
- Docker / Docker Compose 로컬 개발 환경 구성 (`6e6e210`)
- Spring Security + JWT 인증 기반 구현 (`09e90e5`)
- 한국관광공사 Open API 연동 기반 코드 및 공통화 (`d1ce7ff`)
- 행사(Festival) 정보 조회 API 생성 및 테스트 (`73d6c00`)
- `.env` 환경변수 연동 설정 (`bb1ab5b`)
- 임시 랜딩 페이지 (`88d4b47`)
- README 초안 작성 (`aba629d`)
