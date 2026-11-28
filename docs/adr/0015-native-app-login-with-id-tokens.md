# ADR 0015: Native App Login with Provider ID Tokens and Refresh Tokens

## Status
**Accepted** (2026-11-22).

## Context

Android·iOS 앱은 웹앱을 감싼 Flutter WebView 앱이다. 지금 로그인은 서버 쪽 웹 OAuth 흐름(`/oauth2/authorization/kakao`)뿐이고, 결과 JWT 를 URL 쿼리로 넘기며, 10시간 뒤 만료되고 리프레시 토큰이 없다. WebView 안에서는 카카오톡 앱으로 로그인할 수 없고, 백그라운드 위치 업로드는 토큰이 끊기면 함께 끊긴다. iOS 는 제3자 로그인을 쓰면 Apple 로그인도 함께 제공해야 한다(App Store 심사 지침 4.8).

## Decision

1. **앱은 네이티브 SDK 로 로그인한다.** 카카오는 `kakao_flutter_sdk`(카카오톡이 있으면 카카오톡, 없으면 카카오계정), Apple 은 `sign_in_with_apple`. 앱은 받은 OIDC ID 토큰을 `POST /api/auth/{provider}/token` 으로 보낸다.
2. **서버는 ID 토큰을 직접 검증한다.** 제공자 공개키(JWKS)로 서명을 확인하고, iss, aud(우리 앱 키), 만료, nonce 를 본다. 카카오 API 를 매번 부르지 않는다. 카카오 콘솔에서 OpenID Connect 를 켠다.
3. **웹, Android, iOS 는 같은 카카오 앱을 쓴다.** 그래서 카카오 사용자 ID 가 기기와 무관하게 같고, 계정은 (로그인 수단, 그 수단의 사용자 ID)로 찾는다. 이메일로 찾지 않는다.
4. **액세스 토큰(1시간)과 리프레시 토큰(60일)을 따로 둔다.** 리프레시 토큰은 기기마다 발급하고, 쓸 때마다 새것으로 바꾼다. 이미 쓴 리프레시 토큰이 다시 오면 그 기기의 토큰을 모두 폐기한다. 로그아웃은 그 기기의 리프레시 토큰만 폐기한다.
5. **리프레시 토큰은 Flutter 가 보안 저장소에 보관한다.** 웹앱에는 액세스 토큰만 JS 브리지로 넘기고, 웹앱이 401 을 받으면 브리지로 새 토큰을 요청한다.
6. **카카오 연결 끊기 콜백을 받으면 그 사용자의 리프레시 토큰을 모두 폐기한다.** 계정과 기록은 지우지 않는다.
7. **카카오 계정과 Apple 계정은 이번에 잇지 않는다.** 서로 다른 계정이다.
8. **웹 OAuth 로그인(`/oauth2/authorization/kakao`)은 호출한 쪽에 따라 나뉜다.** 네이티브 앱의 WebView 가 User-Agent 에 붙이는 표식(`auth.native-app-user-agent`, 기본 `MaeulApp`)이 있으면 짧게 사는 1회용 코드를 URL 에 실어 돌려보내고, 앱은 그 코드를 액세스 토큰과 리프레시 토큰으로 바꾼다. 표식이 없는 웹 브라우저는 기존 방식대로 액세스 토큰(수명 `auth.token.web-access-ttl`, 기본 10시간)을 `token` 쿼리로 바로 준다. 리프레시 토큰은 없다.
9. **이메일(BASIC) 로그인도 액세스 토큰과 리프레시 토큰을 준다.** 카카오, Apple, 이메일 로그인의 응답 모양이 같다.
10. **기존 10시간 JWT 는 만료될 때까지 인정한다.** 서명키는 바꾸지 않는다.
11. **이번 범위는 서버다.** Flutter 쪽(SDK 연동, 보안 저장소, JS 브리지)은 연동 명세 문서로 넘긴다.

## Considered Options

- **시스템 브라우저 + 딥링크로 기존 웹 흐름 재사용**: 서버 변경은 적지만, 카카오톡 앱 로그인 경험이 떨어지고 카카오와 Apple 의 흐름이 달라진다.
- **WebView 안에서 카카오톡 실행 스킴 가로채기**: 카카오가 공식 지원하지 않아 OS나 카카오톡 업데이트에 깨지기 쉽다.
- **카카오 액세스 토큰을 받아 카카오 API 로 확인**: 로그인마다 외부 호출이 생기고, Apple 과 검증 방식이 갈린다.
- **JWT 만료만 늘리기**: 토큰이 새면 만료까지 막을 방법이 없다.

## Consequences

- 기존 카카오 사용자는 `users.auth_type`/`auth_id` 로 그대로 찾을 수 있다(2026-11-22 개발 DB: KAKAO 1명, auth_id 모두 채워짐, 중복 없음).
- 서버에 리프레시 토큰 저장소와 회전·폐기 규칙이 생긴다.
- Android 키 해시, iOS 번들 ID, Apple Services 설정, 카카오 OIDC 활성화는 콘솔에서 사람이 해야 한다.

## Related ADRs
- ADR 0004: OAuth2 Multi-Provider Support
