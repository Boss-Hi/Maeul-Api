# OAuth2 Multi-Provider 구현 가이드

## 개요
이 문서는 Strategy 패턴과 Factory 패턴을 사용한 다중 OAuth2 provider 지원 시스템의 구현 가이드입니다.

## 아키텍처

### 계층 구조
```
CustomOAuth2UserService (OAuth2 로그인 처리)
         ↓
OAuth2UserInfoFactory (provider별 구현체 생성)
         ↓
OAuth2UserInfo Interface (전략 인터페이스)
         ↓
┌─────────────────────────────────────────┐
│                                         │
KakaoUserInfo  GoogleUserInfo  AppleUserInfo
│                                         │
└─────────────────────────────────────────┘
```

## 핵심 클래스 설명

### 1. OAuth2UserInfo (인터페이스)
```java
public interface OAuth2UserInfo {
    String getProviderId();      // Provider에서의 고유 ID
    String getEmail();           // 사용자 이메일
    String getNickname();        // 사용자 닉네임/이름
    AuthType getAuthType();      // Provider 타입
    Object getAttributes();      // 원본 정보
}
```

**역할**: 모든 provider가 구현해야 할 계약(contract) 정의

### 2. Provider별 구현체

#### KakaoUserInfo
- Kakao의 중첩된 구조 처리 (`kakao_account`, `profile`)
- ID 필드: `id`
- 이메일 필드: `kakao_account.email`
- 닉네임 필드: `kakao_account.profile.nickname`

#### GoogleUserInfo
- Google의 표준 구조 처리
- ID 필드: `sub`
- 이메일 필드: `email`
- 닉네임 필드: `name` 또는 `given_name`

#### AppleUserInfo
- Apple의 제한적인 정보 처리
- ID 필드: `sub`
- 이메일 필드: `email`
- 닉네임: email 기반으로 자동 생성

### 3. OAuth2UserInfoFactory
```java
@Component
public class OAuth2UserInfoFactory {
    public OAuth2UserInfo createUserInfo(String provider, Map<String, Object> attributes) {
        // provider에 따라 적절한 구현체 생성
        return switch (provider.toUpperCase()) {
            case "KAKAO" -> new KakaoUserInfo(attributes);
            case "GOOGLE" -> new GoogleUserInfo(attributes);
            case "APPLE" -> new AppleUserInfo(attributes);
            default -> throw new IllegalArgumentException(...);
        };
    }
}
```

**역할**: Factory 패턴 구현, provider에 따라 올바른 구현체 생성

### 4. CustomOAuth2UserService (리팩토링됨)
```java
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final OAuth2UserInfoFactory oauth2UserInfoFactory;
    
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        // 1. OAuth2 사용자 정보 로드
        OAuth2User oAuth2User = delegate.loadUser(userRequest);
        
        // 2. Factory를 통해 provider별 처리 로직 선택
        OAuth2UserInfo userInfo = oauth2UserInfoFactory.createUserInfo(
            registrationId, 
            oAuth2User.getAttributes()
        );
        
        // 3. 사용자 저장 또는 조회
        saveOrUpdateUser(userInfo);
        
        return new DefaultOAuth2User(...);
    }
}
```

**역할**: OAuth2 로그인 처리, Factory 사용으로 provider 독립적인 코드

## 파일 구조

```
src/main/java/com/bosshi/maeul/auth/
├── dto/
│   ├── OAuth2UserInfo.java                  (인터페이스)
│   ├── KakaoUserInfo.java                   (Kakao 구현체)
│   ├── GoogleUserInfo.java                  (Google 구현체)
│   └── AppleUserInfo.java                   (Apple 구현체)
├── factory/
│   └── OAuth2UserInfoFactory.java           (Factory)
├── service/
│   ├── CustomOAuth2UserService.java         (리팩토링됨)
│   └── CustomUserDetailsService.java        (기존)
├── type/
│   └── AuthType.java                        (KAKAO, GOOGLE, APPLE, BASIC)
└── ...

src/test/java/com/bosshi/maeul/auth/
├── factory/
│   └── OAuth2UserInfoFactoryTest.java       (Factory 테스트)
└── ...
```

## 사용 방법

### 1. 기존 코드 (Before)
```java
if ("kakao".equals(registrationId)) {
    KakaoUserInfo kakaoUserInfo = new KakaoUserInfo(oAuth2User.getAttributes());
    // Kakao 특화 로직
} else if ("google".equals(registrationId)) {
    // Google 로직
}
```

### 2. 개선된 코드 (After)
```java
OAuth2UserInfo userInfo = oauth2UserInfoFactory.createUserInfo(
    registrationId,
    oAuth2User.getAttributes()
);
// 모든 provider에 동일하게 작동
```

## 새로운 Provider 추가 방법

### Step 1: 새로운 구현체 클래스 생성
```java
public class NaverUserInfo implements OAuth2UserInfo {
    private final Map<String, Object> attributes;
    
    public NaverUserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }
    
    @Override
    public String getProviderId() {
        // Naver의 ID 필드 반환
    }
    
    @Override
    public String getEmail() {
        // Naver의 이메일 필드 반환
    }
    
    @Override
    public String getNickname() {
        // Naver의 닉네임 필드 반환
    }
    
    @Override
    public AuthType getAuthType() {
        return AuthType.NAVER;  // AuthType enum에 NAVER 추가 필요
    }
    
    @Override
    public Object getAttributes() {
        return attributes;
    }
}
```

### Step 2: Factory에 케이스 추가
```java
public OAuth2UserInfo createUserInfo(String provider, Map<String, Object> attributes) {
    String upperProvider = provider.toUpperCase().trim();
    
    return switch (upperProvider) {
        case "KAKAO" -> new KakaoUserInfo(attributes);
        case "GOOGLE" -> new GoogleUserInfo(attributes);
        case "APPLE" -> new AppleUserInfo(attributes);
        case "NAVER" -> new NaverUserInfo(attributes);  // 이 줄만 추가!
        default -> throw new IllegalArgumentException(...);
    };
}
```

### Step 3: AuthType enum에 추가
```java
public enum AuthType {
    KAKAO,
    BASIC,
    GOOGLE,
    APPLE,
    NAVER  // 추가
}
```

### Step 4: application.yml에 설정 추가
```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          naver:
            client-id: your-client-id
            client-secret: your-client-secret
            redirect-uri: "{baseUrl}/login/oauth2/code/{registrationId}"
            authorization-grant-type: authorization_code
            scope: profile,email
          provider:
            naver:
              authorization-uri: https://nid.naver.com/oauth2.0/authorize
              token-uri: https://nid.naver.com/oauth2.0/token
              user-info-uri: https://openapi.naver.com/v1/nid/me
              user-name-attribute: response
```

## 디자인 패턴의 장점

### Strategy 패턴의 이점
✅ **행동의 캡슐화**: 각 provider의 처리 로직이 독립적
✅ **런타임 선택**: 실행 시 어떤 전략을 사용할지 결정
✅ **테스트 용이**: 각 전략을 독립적으로 테스트

### Factory 패턴의 이점
✅ **객체 생성의 중앙화**: 모든 생성 로직이 한 곳에
✅ **일관된 인터페이스**: 모든 provider를 동일하게 사용
✅ **유지보수성**: 새 provider 추가 시 변경 범위 최소화

### 조합의 시너지
✅ **Open-Closed Principle**: 기존 코드 수정 없이 확장 가능
✅ **Single Responsibility**: 각 클래스가 하나의 책임만 가짐
✅ **Dependency Inversion**: 구체적 클래스가 아닌 추상화에 의존

## 테스트 전략

### 1. Factory 테스트
```java
@Test
void createKakaoUserInfo() {
    OAuth2UserInfo userInfo = factory.createUserInfo("kakao", attributes);
    assertThat(userInfo).isInstanceOf(KakaoUserInfo.class);
    assertThat(userInfo.getAuthType()).isEqualTo(AuthType.KAKAO);
}
```

### 2. 각 Provider별 테스트
각 provider 구현체마다 자신의 attribute 구조를 정확히 파싱하는지 확인

### 3. 통합 테스트
CustomOAuth2UserService가 provider와 관계없이 일관되게 작동하는지 확인

## 로깅 및 모니터링

```java
log.info(
    "OAuth2 로그인 성공 - Provider: {}, ID: {}, Email: {}",
    provider, userInfo.getProviderId(), userInfo.getEmail()
);

log.debug("{}({}) 사용자 정보 처리", 
    userInfo.getAuthType().name(), userInfo.getProviderId());
```

## 보안 고려사항

1. **AuthType 검증**: 저장된 authType과 로그인 시도 시 authType이 일치하는지 확인
2. **이메일 중복 처리**: 여러 provider에서 같은 이메일로 가입하는 경우 처리
3. **Provider ID 저장**: authId는 provider 내에서 고유하므로 중복 가능
4. **임시 이메일**: 이메일이 없는 provider의 경우 안전한 임시 이메일 생성

## 성능 최적화

1. **Factory는 Stateless**: 상태를 가지지 않으므로 @Component로 싱글톤 관리
2. **Lazy Loading 불필요**: 모든 provider 구현체는 경량
3. **캐싱 불필요**: 매번 새 인스턴스 생성 비용은 무시할 수 있는 수준

## 트러블슈팅

### 문제: "지원하지 않는 OAuth2 provider" 에러
**원인**: registrationId가 Factory에 정의되지 않음
**해결**: application.yml의 client registration 이름이 정확한지 확인

### 문제: 사용자 정보가 제대로 추출되지 않음
**원인**: Provider의 attribute 구조가 예상과 다름
**해결**: 로그를 통해 실제 attribute 구조 확인 후 구현체 수정

### 문제: 같은 이메일의 여러 provider 가입
**원인**: 설계 고려사항
**해결**: 이메일 통합 로직 추가 필요 (별도 ADR 필요)

## 참고 자료

- [Spring Security OAuth2 Documentation](https://spring.io/projects/spring-security)
- [Refactoring Guru - Strategy Pattern](https://refactoring.guru/design-patterns/strategy)
- [Refactoring Guru - Factory Pattern](https://refactoring.guru/design-patterns/factory-method)

