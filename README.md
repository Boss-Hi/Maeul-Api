# Maeul API

**Maeul API**는 지역 커뮤니티와 생활/관광 정보를 한곳에서 제공하기 위한 Spring Boot 기반 백엔드입니다.  
사용자 인증, 게시글 관리, 공공 관광 Open API 연동을 묶어 **동네 중심 서비스의 서버 기반**을 만드는 것이 이 프로젝트의 목표입니다.

## 프로젝트 목표

- 지역 사용자들이 회원가입과 로그인 후 자신의 정보를 확인할 수 있는 인증 API 제공
- 동네 소식, 질문, 정보, 거래, 행사 등을 다루는 게시글 API 제공
- 한국관광공사 Tour API 기반의 축제/숙소/지역 정보 조회 기능 제공
- MySQL, Redis, Docker Compose 환경에서 로컬 개발이 쉬운 구조 유지

## 주요 기능

| 영역 | 설명 |
| --- | --- |
| 인증 | JWT 기반 회원가입, 로그인 |
| 사용자 | 로그인 사용자 정보 조회 (`/api/users/me`) |
| 게시글 | 게시글 생성, 목록 조회, 단건 조회, 수정, 삭제 |
| 외부 연동 | 축제 검색, 숙소 검색, 지역 코드/상세 정보 등 Open API 프록시 |

## 기술 스택

- Java 26
- Spring Boot 4.1
- Spring Security + JWT
- Spring Data JPA
- MySQL
- Redis
- Gradle
- Docker Compose

## 실행 방법

1. 환경 변수 파일 준비

```bash
cp .env.example .env
```

2. 필요하면 `.env`에서 `OPEN_API_SECRET_KEY`를 설정합니다.  
   관광 Open API 호출 기능을 쓰려면 이 값이 필요합니다.

3. 애플리케이션 실행

```bash
./gradlew bootRun
```

또는 Docker Compose로 실행할 수 있습니다.

```bash
docker compose up --build
```

## 기본 설정

`application.yml` 기준 기본 로컬 설정은 아래와 같습니다.

- 서버 포트: `8080`
- MySQL: `jdbc:mysql://localhost:3306/maeul`
- Redis: `localhost:6379`
- JWT 만료 시간: `3600000`ms

## API 경로 예시

- 인증
  - `POST /api/auth/register`
  - `POST /api/auth/login`
- 사용자
  - `GET /api/users/me`
- 게시글
  - `POST /api/posts`
  - `GET /api/posts`
  - `GET /api/posts/{postId}`
- Open API
  - `GET /api/open/search-festival`
  - `GET /api/open/search-stay`
  - `GET /api/open/area-based-list`

## 참고

- `/api/auth/**`와 `/api/open/**`는 인증 없이 접근할 수 있습니다.
- 그 외 API는 JWT 인증이 필요합니다.

