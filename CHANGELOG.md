# Changelog

이 프로젝트의 모든 주요 변경 사항은 이 문서에 기록됩니다.  
버전 규칙은 [Semantic Versioning](https://semver.org/lang/ko/)을 준수합니다.

---

## [Unreleased]

### In Progress

- 첫 오프라인 매장과 상품 종류 등록
  - `FirstStoreSeeder`가 부팅 때 매장 "더온전히 정조효공원점"(경기도 화성시 병점구, `region_code=41`, `subdivision_code=595`)과 상품 종류 5개(아메리카노 1잔, 물고기 친구 분양, Maeul 병아리 스티커, 디저트 1개: 쌀 휘낭시에, 디저트 1개: 쌀 소금빵)를 등록. 같은 이름의 행이 지워진 것까지 포함해 있으면 만들지 않아 운영자가 바꾸거나 지운 것을 되살리지 않음
  - 상품 QR 과 사장님 계정은 만들지 않음. 운영자가 `Prize QR Issue`로 발급하고, 사장님이 가입하면 `Store Owner Add`로 연결
  - "물고기 친구 분양"은 이름만 등록한 상품이며, 펫 종류 추가와 자동 분양은 아직 없음
- 매장 전용 상품
  - 상품 종류를 한 매장 전용으로 묶을 수 있음(`offline_prize_kinds.store_id`, 비어 있으면 어느 매장에서나 지급). 전용 상품 QR 을 다른 매장 사장님이 스캔하면 400 `이 매장에서 지급할 수 없는 상품 QR 입니다.` 로 거절하고 지급·이력을 남기지 않음. 가챠권은 이전처럼 어느 매장에서나 사용 가능
  - 위 5종은 더온전히 정조효공원점 전용. 관리자 `Prize Kind Create` 로는 아직 전용 매장을 지정할 수 없음
- DB
  - `V20`: `offline_prize_kinds.store_id` 추가와 기존 5종을 더온전히 정조효공원점에 묶는 UPDATE. 앱은 부팅할 때 Flyway를 실행하지 않으므로 배포 전에 직접 적용해야 함 (아직 어느 DB에도 적용하지 않음)

## [v0.6.1] - 2026-11-28

> 가챠권이 발급되면 접속 중인 사용자에게 소켓(STOMP)으로 즉시 알림

- 가챠권 발급 알림
  - 일정 완료(`ITINERARY_COMPLETION`)와 사진 미션 성공(`PHOTO_MISSION`)으로 가챠권이 새로 발급되면, 발급이 커밋된 뒤 그 사용자에게만 알림을 보냄. 이미 발급했거나 발급이 꺼져 있어 발급하지 않았을 때, 롤백됐을 때는 보내지 않음
  - 구독 경로 `/private/queue/gacha-ticket-issued`, 페이로드 `{ ticketId, eventType, expiresAt }`. QR 코드(`code`)와 내부 `refKey` 는 담지 않으므로 코드는 가챠권 목록 조회로 받음
  - 접속 중이 아니면 놓치고 다시 보내지 않음. 프론트는 연결·재연결 때 가챠권 목록을 다시 조회해 맞춤
  - 전송이 실패해도 발급과 미션 응답은 그대로이며 ERROR 로그만 남김
  - 연결은 기존 `/ws-stomp`(SockJS)와 `Authorization: Bearer` 인증을 그대로 씀. 새 의존성과 DB 변경 없음

## [v0.6.0] - 2026-11-28

> 화성 정조효공원·융건릉 사진 미션 추가, 웹 OAuth 로그인은 네이티브 앱이 아니면 기존 액세스 토큰 방식으로 복원

- 사진 미션 (ADR 0017)
  - 화성 앵커 일정에 정조효공원·융건릉이 있으면 사진 미션이 일정에 자동으로 붙음. 퀴즈로 기준 사진 3장을 보여 주고, 사용자가 같은 곳 중 하나를 찍어 올려 3장 중 하나라도 맞으면 성공, 성공하면 가챠권 1장(`PHOTO_MISSION`)과 미션 완료(펫이 있으면 미션 가챠)를 받음
  - `GET /api/photo-missions/current`: 진행 중인 일정의 사진 미션(`referencePhotos` 기준 사진 목록, 상태, 오늘 남은 시도 횟수). 없으면 빈 목록
  - `POST /api/photo-missions/{id}/submit`: 사진(`photo`, JPEG/PNG/WebP 5MB 이하)과 현재 위치(`lat`, `lng`)를 올림. 같은 장소로 판정되지 않으면 오류가 아니라 `success: false`
  - `GET /api/photo-missions/reference-photos/{id}/image`: 서버가 서빙하는 기준 사진 이미지(인증 없음)
  - 제출 규칙: 일정 기간 안, 장소 반경 150m 안, 사진 미션당 하루 5회, 같은 사진 해시 거절, EXIF 촬영 시각이 있으면 30분 이내, AI 확신도 0.7 이상. AI가 답하지 못하면 시도로 세지 않고 503
  - 같은 시군구에 기간이 겹치는 일정은 가챠권을 한 번만 줌. 올린 사진은 저장하지 않고 시도 기록에는 위치, 사진 해시, 판정만 남김
  - 기준 사진은 이미지 코드(base64)로 DB에 두고 `PhotoMissionSeeder`가 만듦(코드 `YUNGNEUNG-1~3`). 장소와 기준 사진은 KraftAdmin에서 관리(`PhotoMissionPlace`, `PhotoMissionReferencePhoto`)
  - 가챠권 발급 설정에 이벤트 종류 `PHOTO_MISSION` 추가(켜기·끄기, 유효기간은 관리자가 정함)
- AI 이미지 입력
  - `AiRequest`가 이미지 목록을 받음. Claude(이미지 블록)와 Gemini(inlineData) 모두 지원하고 기존 제공자 순서와 장애 조치를 그대로 씀
- 웹 OAuth 로그인 (ADR 0015 8번 변경)
  - `/oauth2/authorization/kakao` 로그인 성공 후, 네이티브 앱(WebView User-Agent 에 `MaeulApp` 표식)이 아니면 기존 방식대로 액세스 토큰을 `token` 쿼리로 바로 줌(수명 `AUTH_WEB_ACCESS_TOKEN_TTL`, 기본 10시간, 리프레시 토큰 없음)
  - 네이티브 앱 표식이 있을 때만 1회용 `code`를 주고 `POST /api/auth/code/exchange`로 교환. 표식은 `AUTH_NATIVE_APP_USER_AGENT`(기본 `MaeulApp`)로 바꾸며 비우면 모두 `token` 방식
- DB
  - `V18`: 사진 미션 테이블(`photo_mission_places`, `photo_mission_reference_photos`, `photo_missions`, `photo_mission_attempts`), 융건릉 장소 시드, `PHOTO_MISSION` 가챠권 발급 설정
  - `V19`: 기준 사진을 base64 코드로 저장하는 컬럼(`code`, `mime_type`, `image_base64`)과 `photo_missions.reference_photo_id` NULL 허용
  - 앱은 부팅할 때 Flyway를 실행하지 않으므로 V18~V19는 직접 적용해야 함. 서버 DB에는 2026-11-28에 적용 완료
- 설정과 의존성
  - 업로드 상한 `spring.servlet.multipart` 5MB, `photo-mission.*`(확신도, 하루 시도 횟수, EXIF 허용 분, 기준 사진 URL 허용 호스트)
  - EXIF 읽기용 `metadata-extractor` 의존성 추가

## [v0.5.0] - 2026-11-26

> 가챠 시스템(서버 가챠, 오프라인 가챠), 앱 네이티브 로그인, 화성 앵커 일정, 앱 설치 페이지 추가

- 가챠 (ADR 0014)
  - 졸업, 일정 완료, 미션 완료 때 서버가 바로 뽑고 결과를 기록. 졸업은 "상품 교환"이 아니라 졸업 가챠의 결과로 상품을 받을 수 있음
    - 졸업 가챠: 상품, 아이템, 포인트 / 일정 완료 가챠: 아이템, 포인트, 꽝 / 미션 가챠: 펫 경험치 720·360·180
    - 일정 완료는 종료일이 지났고 일정 기간 안에 주축제 시군구 점령 시간이 있는 날이 있으면 완료(매일 스케줄러, 종료일 뒤 7일까지 재판정). 같은 시군구에 겹치는 일정은 한 번만
  - `GET /api/gacha/results`, `GET /api/gacha/results/unseen`, `POST /api/gacha/results/{resultId}/seen`: 내 결과, 미확인 결과, 확인 표시
  - `GET /api/gacha/{gachaType}/odds`: 확률 공개
  - `GET /api/gacha/items`, `GET /api/gacha/vouchers`: 내 아이템(수량), 내 교환권
  - 상품은 교환권으로 주고 재고가 0이면 그 결과를 빼고 남은 결과끼리 뽑음. 가챠로 준 보상은 회수하지 않음
- 오프라인 가챠 (ADR 0016)
  - 일정 완료 때 서버 가챠에 더해 가챠권 1장 발급. 사장님이 가챠권 QR을 스캔해 매장의 가챠 장비를 돌리고, 나온 캡슐의 상품 QR을 스캔해 지급. 결과는 장비가 정하고 서버는 확률을 모름
  - 사용자: `GET /api/gacha/tickets`(상태: 사용 가능, 사용됨, 상품 받음, 만료), `GET /api/gacha/tickets/{ticketId}/qr.png`
  - 사장님(`STORE_OWNER` 역할, 매장 하나에만 소속): `POST /api/store/gacha/tickets/scan`, `POST /api/store/gacha/prizes/scan`(`ticketId`와 함께 보내 연결), `GET /api/store/gacha/history`
  - 관리자(`/api/admin/gacha`, ADMIN 역할): 매장 등록과 사장님 연결, 상품 종류와 상품 QR 대량 발급(최대 1000개)·인쇄용 이미지, 가챠권 사용 취소와 상품 지급 취소(사유 필수), 가챠권 발급 설정(켜기·끄기, 유효기간 기본 30일)
  - 가챠권과 상품 QR은 1회용이고 동시에 스캔해도 한 번만 성공. 상품이 연결된 가챠권은 상품 지급을 먼저 취소해야 취소 가능
  - QR 이미지 생성을 위해 zxing 의존성 추가
- 앱 로그인 (ADR 0015)
  - `POST /api/auth/{provider}/token`: 카카오·Apple ID 토큰으로 로그인. 서버가 제공자 공개키로 직접 검증
  - 액세스 토큰 1시간, 리프레시 토큰 60일(기기별, 사용할 때마다 교체). `POST /api/auth/token/refresh`, `POST /api/auth/logout`(그 기기만 폐기)
  - `POST /api/auth/code/exchange`: 웹 브라우저 OAuth는 URL에 JWT 대신 1회용 코드를 돌려주고 이 API로 토큰과 교환
  - `/api/auth/kakao/unlink`: 카카오 연결 끊기 콜백. 그 사용자의 리프레시 토큰을 모두 폐기하고 계정과 기록은 유지
  - 계정은 (로그인 수단, 그 수단의 사용자 ID)로 찾음. 기존 10시간 JWT는 만료될 때까지 유효
- 화성 앵커 일정
  - 주축제의 시군구가 화성시(또는 그 아래 구 4곳)이면 주변 장소 추천 대신 화성 앵커 장소 8곳(동탄 호수공원·동탄역, 정조효공원·융건릉, 더온전히 정조효공원점, 전곡항·서해랑 케이블카, 황금해안길, 서해마루 유스호스텔, 궁평항 에코팜랜드 캠핑장, 궁평항 수산시장)을 뼈대로 일정 생성
  - 선택한 주축제는 개최 기간 안에 정확히 1회 일정에 녹임. 앵커 장소는 `contentId` 없는 일반(NORMAL) 항목, 주축제는 TOUR 항목
  - 여행 기간에 맞게 앵커 장소를 늘리거나 줄이고, 숙박은 마지막 날을 뺀 매일 밤 하나씩, 낙조 같은 시간대 역할은 그 시간대에 배치
  - 보조 후보는 화성시 안의 숙박이 아닌 장소만. 같은 날 같은 앵커 장소가 반복되면 다음 AI 제공자로 다시 요청하고, 남은 제공자가 없으면 그 일정을 사용
  - 화성이 아닌 지역은 기존 주축제 중심 일정 그대로
- 앱 설치 페이지
  - `GET /app`: QR이 가리키는 고정 주소. 모바일은 기기에 맞는 스토어(Android는 Google Play, iOS는 App Store)로 2초 뒤 자동 이동, PC는 두 스토어 버튼, 카카오톡 등 인앱 브라우저는 기본 브라우저로 여는 안내
  - 스토어 링크는 `APP_STORE_ANDROID_URL`, `APP_STORE_IOS_URL` 환경변수. 비어 있는 플랫폼은 "준비 중". `src` 쿼리는 로그로만 기록
- DB
  - `V15`: 가챠 테이블(`prizes`, `items`, `gacha_outcomes`, `gacha_results`, 교환권, 보유 아이템 등)
  - `V16`: 리프레시 토큰 테이블
  - `V17`: 오프라인 가챠 테이블(`stores`, `store_owners`, `gacha_ticket_rules`, `gacha_tickets`, `offline_prize_kinds`, `prize_qrs`, `offline_gacha_logs`)
  - 앱은 부팅할 때 Flyway를 실행하지 않으므로 V15~V17은 직접 적용해야 함
- 사용자 역할 `STORE_OWNER` 추가. 사장님은 관리자 API로 매장에 연결하고 사용자 역할도 따로 지정해야 스캔 가능

## [v0.4.0] - 2026-11-15

> 펫, 주인 기능 추가, 일정 생성 후 해당 일정의 일정 기간 안의 머문 시간으로 경험치 계산, 주인 경험치 합산

- 펫(fat) (시군구마다 머문 시간으로 키우는 캐릭터, ADR 0010)
  - `GET /api/fats`: 키우는 중인 펫(레벨, 단계 이름, 경험치, 다음 레벨까지, 교환 가능 여부), 동시에 키울 수 있는 수(`maxActiveFats`), 추천 분양 지역(`suggestedAdoptionRegions`)
    - 추천 분양 지역: 오늘 머물렀거나, 오늘 펫이 졸업했거나, 아직 끝나지 않은 내 일정의 시군구 중 펫이 없는 곳
  - `GET /api/fats/regions`: 분양받을 수 있는 시군구 전체. 아래에 구가 있는 시(수원시 등 13곳)는 제외
  - `POST /api/fats` (`{ regionCode, code }`): 분양
    - 가 본 적이 없어도 전국 어느 시군구에서나 미리 받을 수 있음 (ADR 0011)
    - 사용자마다 시군구당 1마리. 최대 마리 수에 이르렀거나, 없는 시군구이거나, 구가 있는 시면 400
  - `POST /api/fats/{fatId}/graduate`: 졸업(상품 교환). 건국 레벨부터 가능, 되돌리지 않음
  - `GET /api/fats/graduated?page=`: 졸업한 펫 (졸업 때 레벨·경험치). 최근 졸업 순 페이지 (기본 size 10)
  - `GET /api/fats/ranking?regionCode=&code=&page=&size=`: 지역별 펫 순위 (키우는 중인 펫의 지금 경험치 순)
    - `entries` 는 그 지역(없으면 전국)의 상위 100마리(`TERRITORY_RANKING_MAX_SIZE`)만 나눈 페이지 (기본 size 10)
    - `regionCode`+`code` 는 시군구, `regionCode` 만 있으면 시도, 둘 다 없으면 전국
    - 같은 경험치는 같은 순위(1, 1, 3), 닉네임만 노출, `mine` 은 내 펫 중 가장 높은 순위
- 펫 시군구 (ADR 0012)
  - `region_subdivisions` 의 한 행. `region_code` 와 `code` 를 각각 받고 이어 붙이지 않음 (세종은 `36110`/`36110`)
  - 응답의 시군구 필드: `regionSubdivisionId`, `regionCode`, `code`, `sidoName`, `sigunguName`
  - 점령 시간과는 카카오가 판정한 시도·시군구 이름(`territory_region_cells`)을 `regions.name`·`region_subdivisions.name` 과 맞춰 연결
- 펫 경험치 (ADR 0013)
  - 경험치 = 분양 이후, 그 시군구 일정 기간 안의 머문 시간(1분 = 1) + 방문일(하루 1회, 기본 60, `fat_settings.visit_day_exp`)
  - 일정 기간은 그 시군구(주축제 기준)로 만든 일정의 시작일 0시~종료일 24시. 일정 밖에서 머문 시간은 세지 않음
  - 위치 기록을 지우면 함께 줄고, 레벨은 지금 레벨표로 매번 계산
  - 일정을 만들거나 날짜·주축제를 바꾸면 일정 모듈이 `ItineraryPeriodChangedEvent` 를 보내고, 점령 모듈이 `fat_itinerary_periods` 에 기간 복사본을 저장
- 주인 (Master)
  - 사용자마다 1명. 주인 경험치 = 모든 펫 경험치 합(키우는 중은 지금 값, 졸업은 졸업 때 값) x 주인 비율(`fat_settings.master_exp_ratio`, 기본 1.0)
  - `GET /api/master`: 내 주인의 레벨·경험치
  - `GET /api/master/ranking?regionCode=&code=&page=&size=`: 지역별 주인 순위. 그 지역에서 펫을 키우는 중이거나 졸업시킨 사용자를 주인 경험치 전체 순으로 줄 세움
    - `entries` 는 그 지역(없으면 전국)의 상위 100명(`TERRITORY_RANKING_MAX_SIZE`)만 나눈 페이지 (기본 size 10)
- 관리자 설정 (KraftAdmin)
  - `FatLevel`: 펫 레벨표(`fat_levels`)의 레벨별 필요 경험치·단계 이름·건국 여부. 기본 Lv.1~100, 레벨마다 126분, Lv.81 건국(10,080분)
  - `MasterLevel`: 주인 레벨표(`master_levels`). 기본 Lv.1~100, 레벨마다 1,008
  - `FatSetting`: 동시에 키울 수 있는 펫 수(`max_active_fats`, 기본 3. 줄여도 이미 키우는 펫은 그대로), 방문일 경험치, 주인 비율
- DB
  - `fat_levels`, `fat_settings`, `fats` 테이블 추가 (V12)
  - `fat_itinerary_periods` 테이블과 `fat_settings.visit_day_exp` 추가 (V13)
  - `master_levels` 테이블과 `fat_settings.master_exp_ratio` 추가 (V14)
  - 이전 V12~V15 를 적용한 개발 DB 는 펫 테이블과 `kakao_legal_dong_codes` 를 지우고, `flyway_schema_history` 의 12 이상 기록을 지운 뒤 다시 migrate

## [v0.3.1] - 2026-11-08

- API 추가
  - 일정 생성 전 추가 질문 (Itinerary Settings (일정 생성 전 추가 설정))
  - 설문 답변 요약 조회 (Survey Summary (설문 응답 요약))
- 일정 생성 시 회원 정보 및 추가 질문 반영
  - 추가 질문 사항 DB 저장
- 설문 종류 정리
  - 설문 데이터 값을 정형화 하여 일정 생성에 쓰도록 수정
- 코드 정리 
  - service 코드 정리
  - 일정 패키지 정리
  - ai 패키지 정리
    - claude 기본 모델 수정
    - ai provider 정리

## [v0.3.0] - 2026-11-07

- 위치 기록 타임라인 (Google 타임라인 방식, ADR 0007)
  - `GET /api/timeline/settings`: 수집 주기 등 앱 설정 조회
  - `POST /api/timeline/points`: 위치 기록 업로드 (최대 1440개, 오프라인 동안 쌓인 기록 일괄 전송)
  - `GET /api/timeline?date=YYYY-MM-DD`: 하루 타임라인 (머문 장소 `STAY` / 이동 `MOVE` 구간)
  - `DELETE /api/timeline?date=YYYY-MM-DD`: 하루 위치 기록 삭제
  - 로그인 사용자 본인 기록만 다룸
  - 수집 주기 기본 1분, `TIMELINE_COLLECTION_INTERVAL` 로 변경 (주기보다 촘촘한 기록은 저장하지 않음)
  - `timeline_points` 테이블 추가 (V9)
- 점령 (위치 기록 기반 시군구별 누적 시간, ADR 0008)
  - `GET /api/territory/me?month=YYYY-MM`: 현재 방문(currentVisit), 시군구별 누적 점령 시간·개인 순서·월간 순위, 포인트 적립 진행
  - `GET /api/territory/regions?parentRegionId=`: 지역 목록 (전국 `KR` / 시도 2자리 / 시군구 5자리)
  - `GET /api/territory/regions/{regionId}/ranking?month=`: 시군구 월간 점령 순위
  - `GET /api/points`: 포인트 잔액과 내역. 점령 1시간마다 100P (`TERRITORY_REWARD_UNIT`, `TERRITORY_REWARD_POINTS`)
  - 시군구 판정은 카카오 역지오코딩 + 약 100m 격자 캐시, 기록이 3분(`TERRITORY_MAX_GAP`) 넘게 끊긴 시간은 점령 시간에서 제외
  - `territory_region_cells`, `territory_daily_stays`, `point_transactions` 테이블 추가 (V10)
- 필수 파라미터 누락, 파라미터 형식 오류, 읽을 수 없는 요청 본문을 500 대신 400 으로 응답

## [v0.2.0] - 2026-11-07

> AI 일정 생성 기능 개선 및 제공자 다중화
 
- Claude AI 추가 및 일정 생성 AI 제공자 다중화 (ADR 0006)
  - 제공자 순서(`AI_PROVIDERS`, 기본 `claude,gemini`)대로 시도하고 실패하면 다음 제공자로 대체
  - Claude: 공식 Java SDK, 기본 모델 `claude-opus-5-5`(effort medium), structured outputs, 거절 시 서버 측 대체(`fallbacks: default`)
  - Claude 키도 `ai_api_keys`(provider_type=CLAUDE)로 관리, 401/403 거부 키 자동 비활성화
  - 키 조회를 제공자별로 분리 (기존에는 다른 제공자의 키도 Gemini 가 사용할 수 있었음)
  - 테스트 생성기를 `test` 제공자로 전환 (`@Qualifier` 하드코딩 제거)
  - 일정 생성 요청 API 는 모든 제공자에 활성 키가 없을 때만 503

- 설문 답변 수정 api 수정
  - questionId, optionId 로 설문 답변을 재생성
  - answerId 로 설문 답변을 수정하는 api 는 삭제
- 축제 목록
  - 진행 중, 종료된, 모든 축제 보기 필터 추가
- 일정 생성 AI 키 오류 대응
  - 사용 가능한 AI 키가 없으면 일정 생성 요청 시 즉시 503 응답
  - Gemini 가 거부한 키(API_KEY_INVALID, PERMISSION_DENIED)는 자동 비활성화 후 다음 키로 재시도
  - 설정 파일 키로의 fallback 제거 (DB `ai_api_keys` 만 사용)
  - 일정 생성 결과 웹소켓 메시지 형식 변경 (`{ status, itineraryId }` / `{ status, message }`), 실패 시에도 알림
  - 에러 로그의 API 키 마스킹
  - AI 키가 업데이트(lastUsedAt, active)마다 중첩 암호화되던 버그 수정 (EntityListener → `@Convert`)
  - Gemini 일시적 오류(500, 503 high demand, 504) 지수 백오프 재시도 및 대체 모델(`GEMINI_FALLBACK_MODEL`) 지원
- 일정 생성 후 미션 생성 시 LazyInitializationException 수정 (미션 생성 트랜잭션에서 일정 재조회)
- AI 일정 생성 시 contentId 를 선택 사항으로 변경
  - 추천 목록의 contentId 는 TOUR 상세 일정, 그 외(contentId 없음/알 수 없음)는 title·description 으로 NORMAL 상세 일정 생성
  - 프롬프트 응답 스키마에 title, description 추가

## [v0.1.5] - 2026-11-01

> 저장한 설문 response 에 id 값 추가

### Fixed
- 저장한 설문 response 에 id 값 추가

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
