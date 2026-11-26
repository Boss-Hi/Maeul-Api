# 앱·웹 로그인 연동 명세 (ADR 0015)

Flutter 앱(웹앱을 감싼 WebView)과 웹앱이 서버 로그인 API를 쓰는 방법이다. 결정 배경은 `docs/adr/0015-native-app-login-with-id-tokens.md`에 있다.

## 토큰

모든 로그인과 갱신은 같은 응답을 준다.

```json
{
  "success": true,
  "message": "success",
  "data": {
    "accessToken": "eyJ...",
    "tokenType": "Bearer",
    "accessTokenExpiresIn": 3600,
    "refreshToken": "q3V...",
    "refreshTokenExpiresAt": "2027-01-21T10:00:00"
  }
}
```

- API 호출: `Authorization: Bearer {accessToken}`. 액세스 토큰은 1시간 산다.
- 리프레시 토큰은 60일 살고, 기기 로그인마다 따로 있다. 갱신하면 새 리프레시 토큰이 오고, 보낸 토큰은 더 쓸 수 없다. 반드시 응답의 새 토큰으로 바꿔 저장한다.
- 이미 쓴 리프레시 토큰을 다시 보내면 그 기기의 로그인이 모두 폐기된다. 두 곳에서 동시에 갱신하지 않도록 갱신은 한 곳(Flutter)에서만, 한 번에 하나씩 한다.
- 갱신이 401 이면 다시 로그인한다.

## API

| 메서드 | 경로 | 본문 | 설명 |
|---|---|---|---|
| POST | `/api/auth/kakao/token` | `{ idToken, nonce }` | 앱 카카오 로그인 |
| POST | `/api/auth/apple/token` | `{ idToken, nonce, nickname? }` | 앱 Apple 로그인 (iOS) |
| POST | `/api/auth/login` | `{ email, password }` | 이메일 로그인 (응답이 위 토큰 모양으로 바뀜) |
| POST | `/api/auth/token/refresh` | `{ refreshToken }` | 토큰 갱신 |
| POST | `/api/auth/logout` | `{ refreshToken }` | 이 기기 로그아웃 |
| POST | `/api/auth/code/exchange` | `{ code }` | 웹 로그인 코드를 토큰으로 교환 |

ID 토큰 검증 실패, 만료·폐기·재사용된 리프레시 토큰, 잘못된 코드는 모두 401 이다.

## Flutter 앱

### 카카오 (`kakao_flutter_sdk_user`)

1. 웹과 같은 카카오 앱의 **네이티브 앱 키**로 SDK 를 초기화한다.
2. 매 로그인마다 무작위 nonce 원문을 만든다.
3. 카카오톡이 설치되어 있으면 `UserApi.instance.loginWithKakaoTalk(nonce: nonce)`, 아니면 `loginWithKakaoAccount(nonce: nonce)`.
4. 받은 `OAuthToken.idToken` 과 nonce **원문**을 `POST /api/auth/kakao/token` 에 보낸다.
5. 로그아웃할 때는 서버 `/api/auth/logout` 뒤 `UserApi.instance.logout()` 도 부른다.

### Apple (`sign_in_with_apple`, iOS)

1. nonce 원문을 만들고 SHA-256 hex 를 `nonce:` 에 넘긴다 (Apple 토큰에는 해시가 들어간다).
2. `getAppleIDCredential(scopes: [fullName], nonce: sha256(raw))`.
3. `identityToken`, nonce **원문**, 그리고 처음 로그인이면 받은 이름을 `nickname` 으로 `POST /api/auth/apple/token` 에 보낸다. Apple 은 이름을 첫 로그인 때 한 번만 준다.

### 토큰 보관과 웹앱 전달

- 리프레시 토큰은 `flutter_secure_storage`(Keychain/Keystore)에만 둔다. WebView 저장소(localStorage, 쿠키)에 넣지 않는다.
- 액세스 토큰은 JS 브리지로 웹앱에 넘긴다. 권장 브리지:
  - Flutter → 웹: 로그인·갱신 뒤 `window.maeulAuth.setAccessToken(token)` 호출
  - 웹 → Flutter: 웹앱이 401 을 받으면 `MaeulAuth.postMessage('refresh')`. Flutter 는 갱신 후 새 액세스 토큰을 넘기고, 실패하면 로그인 화면으로 보낸다.
- 백그라운드 위치 업로드처럼 Flutter 가 직접 서버를 부를 때도 같은 토큰을 쓴다.

## 웹 브라우저

1. 로그인 버튼은 `/oauth2/authorization/kakao` 로 보낸다.
2. 로그인이 끝나면 서버가 `SPRING_OAUTH_REDIRECT_URL?code={1회용 코드}` 로 돌려보낸다. 예전처럼 `?token=` 이 오지 않는다.
3. 웹앱은 1분 안에 `POST /api/auth/code/exchange` 로 코드를 토큰으로 바꾸고, URL 에서 code 를 지운다(`history.replaceState`). 코드는 한 번만 쓸 수 있다.

## 콘솔 설정 (사람이 할 일)

- 카카오 디벨로퍼스 (웹이 쓰는 앱과 같은 앱)
  - 카카오 로그인 > OpenID Connect 활성화
  - 플랫폼: Android 패키지명·키 해시, iOS 번들 ID 등록
  - 카카오 로그인 > 연결 끊기 알림 콜백: `https://{서버}/api/auth/kakao/unlink`
- Apple Developer: App ID 에 Sign in with Apple 활성화

## 서버 환경 변수

| 변수 | 설명 |
|---|---|
| `KAKAO_NATIVE_APP_KEY` | 앱 카카오 ID 토큰의 aud |
| `KAKAO_REST_API_KEY` | 웹 카카오 ID 토큰의 aud (기존 값) |
| `KAKAO_ADMIN_KEY` | 연결 끊기 콜백의 `Authorization: KakaoAK {키}` 확인 |
| `APPLE_BUNDLE_ID` | 앱 Apple ID 토큰의 aud |
| `APPLE_SERVICES_ID` | 웹 Apple 로그인용 (쓰지 않으면 비워 둠) |
| `AUTH_ACCESS_TOKEN_TTL`, `AUTH_REFRESH_TOKEN_TTL`, `AUTH_LOGIN_CODE_TTL` | 기본 1h, 60d, 1m |

`JWT_EXPIRATION_MS` 는 더 쓰지 않는다. 배포 전에 발급된 10시간 토큰은 서명키가 같아 만료될 때까지 그대로 쓸 수 있다.
