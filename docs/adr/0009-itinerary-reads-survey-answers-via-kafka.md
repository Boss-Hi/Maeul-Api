# ADR 0009: Itinerary Reads Survey Answers via Kafka Request/Reply

## Status
**Accepted** (2026-11-08)

## Context

일정 생성(`/api/itinerary/generate`, `/api/itinerary/kafka/generate`)에 사용자가 설문에 입력한 값(나이대, 직업, 선호 숙소, 테마, 일정 템포 등)과 하루 일정 설정(페이스, 하루 시간대)을 반영해야 한다.
설문 데이터는 `survey` 모듈이 소유한다. `itinerary` 가 `survey` 의 엔티티나 리포지토리를 직접 import 하면 모듈 경계 정책(다른 모듈의 내부를 import 하지 않는다)에 어긋난다.
결정해야 할 것:
- `itinerary` 가 `survey` 의 답변을 어떤 방식으로 가져올지
- 가져오지 못했을 때 어떻게 할지

## Decision

1. **Kafka 요청/응답(`ReplyingKafkaTemplate`).** `itinerary` 가 `survey-answers-request`(`userId`)를 보내고, `survey` 의 리스너가 `survey-answers-reply` 로 답변 목록을 돌려준다. 응답이 오면 일정 생성을 이어간다.
   동기 엔드포인트와 Kafka 엔드포인트 모두 `ItineraryService.generateItinerary` 를 지나므로 같은 경로를 쓴다.
2. **메시지 계약은 `survey` 가 소유한다.** `survey/kafka` 의 `SurveyAnswersRequest`, `SurveyAnswersReply`, `SurveyTopic` 을 `@NamedInterface` 로 열어 둔다. `itinerary` 는 자기 소유의 `SurveyAnswerClient` 인터페이스(구현: `KafkaSurveyAnswerClient`)와 `SurveyAnswerInfo` 를 쓰고, survey 타입은 구현체 안에만 둔다.
3. **현재 응답 = 활성 설문에 대한 사용자의 최신 `SurveyResponse`.** 응답이 없으면 빈 목록이며, 프롬프트에서 설문 부분을 뺀다.
4. **가져오지 못하면 일정 생성을 실패시킨다.** 응답 대기 5초(`ReplyingKafkaTemplate` 기본 타임아웃), 전송 대기(`max.block.ms`) 2초, 파티션 할당 대기 5초를 넘기면 `ServiceUnavailableException` 이다. 동기 엔드포인트는 503, Kafka 엔드포인트는 WebSocket 으로 FAILED 를 보낸다.
5. **페이스·시간대는 `ItineraryService` 가 채운다.** 페이스는 요청, 설문의 일정 템포(`relaxed|moderate|packed`), MODERATE 순이다. 하루 시간대의 기본값은 09:30 ~ 22:30 이다.

## Rationale

- 직접 호출(`@NamedInterface` 서비스)이 더 단순하지만, 이 프로젝트는 모듈 간 통신을 메시지로 두는 방향을 택했다. 나중에 `survey` 를 분리해도 `itinerary` 코드는 `SurveyAnswerClient` 구현만 바뀐다.
- 요청/응답은 상관관계(correlation)를 라이브러리가 처리하므로, 이벤트 체인과 달리 일정 생성 중간 상태(보류 중인 DTO)를 저장할 필요가 없고 두 엔드포인트가 같은 경로를 쓴다.

## Consequences

### 긍정적 영향
- `itinerary` 가 `survey` 의 엔티티·리포지토리를 import 하지 않는다.
- 설문 답변 형식이 바뀌어도 프롬프트 생성은 질문 제목과 옵션 라벨을 그대로 쓰므로 질문 번호에 묶이지 않는다.

### 부정적 영향
- `/api/itinerary/generate` 가 Kafka 브로커와 `survey` 리스너에 의존한다. 둘 중 하나가 내려가면 503 이다. 이전에는 브로커 없이 동작했다.
- 응답을 기다리는 동안 요청 스레드(동기) 또는 소비자 스레드(Kafka)가 최대 수 초 막힌다.
- 새 소비자 그룹은 브로커의 초기 리밸런스 지연(약 3초) 뒤에 파티션을 받으므로, 앱 시작 직후 약 3초 안의 첫 요청은 그만큼 늦어진다.
- 응답 컨테이너가 인스턴스마다 고유한 그룹으로 응답 토픽을 읽는다. 다른 인스턴스의 응답은 상관관계 ID 가 달라 버려진다.

## Related ADRs
- ADR 0006: Multi-AI Provider Failover
