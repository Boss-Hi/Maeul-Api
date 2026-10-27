# Conventional Commits 가이드라인

본 문서는 프로젝트의 명확한 이력 관리와 자동화된 버전 생성을 위한 **Conventional Commits** 표준 메시지 구조 및 작성 규칙을 정의합니다.

---

## 1. 커밋 메시지 표준 구조

커밋 메시지는 **Header(제목)**, **Body(본문)**, **Footer(꼬리말)** 3개의 주요 영역으로 구성되며, 각 영역 사이는 **빈 줄 한 줄**로 구분합니다.

```text
<type>(<scope>): <short description>

[optional body]

[optional footer(s)]
```

### 1) Header (필수)
* **`type` (필수):** 커밋의 변경 성격을 나타내는 키워드 (소문자 작성).
* **`scope` (선택):** 변경이 이루어진 도메인 또는 모듈명 (예: `auth`, `user`, `config`).
* **`short description` (필수):** 변경 사항에 대한 명확하고 간결한 요약.
  * 50자 이내 작성 권장.
  * 마침표(`.`)를 사용하지 않음.
  * 과거형 대신 **명령문/동사 원형** 사용 (예: `Add`, `Fix`, `Change`).

### 2) Body (선택)
* **'무엇을'**, **'왜'** 변경했는지에 대해 구체적으로 기술.
* 변경 전의 동작과 변경 후의 동작 차이 설명.
* 한 줄당 72자 이내로 작성 권장.

### 3) Footer (선택)
* **이슈 추적:** 연관된 이슈/PR 번호 명시 (예: `Closes #123`, `Fixes #45`).
* **주요 변경 사항(Breaking Changes):** 하위 호환성을 깨뜨리는 API 변경 등이 발생한 경우 명시.

---

## 2. 모든 커밋 타입(Type) 분류

| 타입 (Type) | 의미 (Meaning) | 설명 (Description) | SemVer 연동 |
| :--- | :--- | :--- | :--- |
| **`feat`** | Feature | 새로운 기능 추가 | **MINOR** |
| **`fix`** | Bug Fix | 버그 및 에러 수정 | **PATCH** |
| **`docs`** | Documentation | 문서 수정 (README, Swagger, 주석 등) | - |
| **`style`** | Code Style | 코드 구조 변경 없는 포맷팅, 세미콜론 누락, 들여쓰기 수정 | - |
| **`refactor`** | Refactoring | 버그 수정이나 기능 추가 없는 코드 구조 개선 | - |
| **`perf`** | Performance | Performance 향상을 위한 코드 개선 | **PATCH** |
| **`test`** | Test | 테스트 코드 추가, 수정, 리팩토링 | - |
| **`build`** | Build System | 빌드 설정 파일 및 외부 의존성 관련 수정 (Gradle, Maven 등) | - |
| **`ci`** | Continuous Integration | CI/CD 파이프라인 설정 수정 (GitHub Actions, Docker 등) | - |
| **`chore`** | Maintain | 빌드 업무, 패키지 매니저 설정 등 자잘한 유지보수 작업 | - |
| **`revert`** | Revert | 이전 커밋을 취소(복구)하는 작업 | - |

---

## 3. 하위 호환성을 깨뜨리는 변경 (`BREAKING CHANGE`)

기존 API 규격 변경 등으로 인해 하위 호환성이 깨지는 작업이 포함된 경우 SemVer의 **MAJOR** 버전을 증가시켜야 합니다.

### 작성 방식 (2가지 중 선택)
1. **Header 타입 뒤에 `!` 붙이기:** `feat(api)!: change user response payload format`
2. **Footer에 `BREAKING CHANGE:` 기술하기**

---

## 4. 실무 작성 예시 (Examples)

### 예시 1: 단문 커밋 (Header만 사용)
```text
feat(itinerary): add itinerary schedule reordering API
```

### 예시 2: 버그 수정 커밋 (Header + Body + Footer)
```text
fix(auth): return 401 Unauthorized instead of 500 on JWT expiration

Previously, expired JWT tokens caused an unhandled Exception resulting in 
500 Internal Server Error. Updated GlobalExceptionHandler to handle 
ExpiredJwtException properly and return 401 response.

Closes #38
```

### 예시 3: 주요 변경 사항 포함 커밋 (`BREAKING CHANGE`)
```text
feat(api)!: change user response format from XML to JSON

BREAKING CHANGE: The endpoint /api/v1/users now returns JSON instead of XML.
Existing clients using XML parsers must update their integration logic.

Fixes #102
```