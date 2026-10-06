# ADR-0004: OAuth2 Multi-Provider Support Using Strategy and Factory Patterns

## Status
Accepted

## Context
기존 코드에서는 Kakao OAuth2 provider만 지원하고 있었습니다. 프로젝트의 요구사항에 따라 Google, Apple 등 여러 OAuth2 provider를 동시에 지원해야 하는 상황이 발생했습니다.

### 문제점
- Kakao에만 특화된 로직이 `CustomOAuth2UserService`에 하드코딩되어 있음
- 새로운 provider를 추가하려면 서비스 클래스를 직접 수정해야 함 (Open-Closed Principle 위반)
- provider별 사용자 정보 처리 로직이 분산되어 있어 유지보수가 어려움

## Decision
**Strategy 패턴**과 **Factory 패턴**을 결합하여 다음과 같이 설계했습니다:

### 1. Strategy 패턴 - OAuth2UserInfo 인터페이스
```java
public interface OAuth2UserInfo {
    String getProviderId();
    String getEmail();
    String getNickname();
    AuthType getAuthType();
    Object getAttributes();
}
```

각 provider(Kakao, Google, Apple)는 이 인터페이스를 구현하여 자신만의 사용자 정보 추출 로직을 정의합니다.

**장점:**
- 새로운 provider 추가가 매우 간단 (새로운 구현체만 추가)
- 각 provider의 로직이 캡슐화되어 있음
- 테스트가 용이함

### 2. Factory 패턴 - OAuth2UserInfoFactory
```java
@Component
public class OAuth2UserInfoFactory {
    public OAuth2UserInfo createUserInfo(String provider, Map<String, Object> attributes) {
        return switch (provider.toUpperCase()) {
            case "KAKAO" -> new KakaoUserInfo(attributes);
            case "GOOGLE" -> new GoogleUserInfo(attributes);
            case "APPLE" -> new AppleUserInfo(attributes);
            default -> throw new IllegalArgumentException(...);
        };
    }
}
```

**장점:**
- provider에 따라 올바른 구현체를 생성하는 책임을 분리
- 생성 로직이 한 곳에 집중되어 있음
- 새로운 provider 추가 시 이 클래스만 수정하면 됨

## Implementation

### 파일 구조
```
src/main/java/com/bosshi/maeul/auth/
├── dto/
│   ├── OAuth2UserInfo.java          (인터페이스)
│   ├── KakaoUserInfo.java           (구현체)
│   ├── GoogleUserInfo.java          (구현체)
│   └── AppleUserInfo.java           (구현체)
├── factory/
│   └── OAuth2UserInfoFactory.java   (Factory)
└── service/
    └── CustomOAuth2UserService.java (리팩토링됨)
```

### 주요 변경사항

1. **OAuth2UserInfo 인터페이스 추가**
   - 모든 provider가 구현해야 할 메서드 정의
   - provider별 차이를 추상화

2. **Provider별 구현체 추가**
   - `KakaoUserInfo`: Kakao의 중첩된 구조 처리
   - `GoogleUserInfo`: Google의 사용자 정보 매핑
   - `AppleUserInfo`: Apple의 제한적인 정보 처리

3. **OAuth2UserInfoFactory 추가**
   - provider 문자열로부터 올바른 구현체를 생성
   - 대소문자 무시
   - 잘못된 provider에 대해 명확한 예외 발생

4. **CustomOAuth2UserService 리팩토링**
   - Kakao 특화 로직 제거
   - Factory를 통해 provider 독립적인 처리
   - `saveOrUpdateUser` 메서드로 공통 로직 추출

## Consequences

### 긍정적 결과
✅ **확장성**: 새로운 provider 추가가 매우 간단
✅ **유지보수**: 각 provider의 로직이 독립적으로 관리됨
✅ **테스트**: 각 구현체와 factory를 독립적으로 테스트 가능
✅ **SOLID 원칙 준수**:
   - Single Responsibility: 각 클래스가 하나의 책임만 가짐
   - Open-Closed: 기존 코드 수정 없이 새 provider 추가 가능
   - Liskov Substitution: 모든 OAuth2UserInfo 구현체가 대체 가능
   - Dependency Inversion: 추상화에 의존

### 고려사항
⚠️ **새로운 클래스 증가**: 각 provider마다 새로운 클래스 추가
⚠️ **Factory 유지보수**: 새 provider 추가 시 factory도 함께 수정 필요

## Alternatives Considered

### 1. Reflection 사용
provider 이름으로 클래스를 동적으로 로드
- ❌ 성능 오버헤드, 테스트 어려움, 타입 안정성 부족

### 2. 상속 기반 설계
`BaseOAuth2UserInfo` 추상 클래스 사용
- ❌ Strategy 인터페이스가 더 유연함

## Related ADRs
- ADR-0001: Real-time API Redis Caching
- ADR-0002: Main Festival Geo Filtering

## References
- Strategy Pattern: https://refactoring.guru/design-patterns/strategy
- Factory Pattern: https://refactoring.guru/design-patterns/factory-method
- SOLID Principles: https://en.wikipedia.org/wiki/SOLID

