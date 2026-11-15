# ADR 0012: Fat Region from a Kakao Legal-dong Code Table

## Status
**Accepted** (2026-11-15)

## Context

병아리는 전국 어느 시군구에서나 미리 분양받는다(ADR 0011). 그러려면 시군구 목록과 식별자가 필요하다. 경험치는 점령 시간에서 계산하는데, 점령 시간은 카카오가 위치마다 돌려주는 법정동 코드의 앞 5자리로 쌓인다(ADR 0008).
처음에는 일정 쪽의 `region_subdivisions`(한국관광공사 기준)를 쓰려 했다. 그러나 다음 두 가지 때문에 맞지 않았다.
- 세종특별자치시는 `region_code=36110`, `code=36110` 이다. `region_code` 와 `code` 를 붙인 값이 시군구 코드가 되지 않는다.
- 두 API 의 코드와 이름이 늘 같다는 보장이 없다.

## Decision

1. **병아리의 시군구는 `kakao_legal_dong_codes` 의 한 행이다.** `fats.legal_dong_code_id` 가 이 행을 가리킨다. 행의 `code` 는 점령의 `region_id` 와 같은 값이어서, 경험치를 셀 때 따로 변환하지 않는다.
2. **이 표는 미리 채운다.** 행정안전부 행정표준코드관리시스템(code.go.kr)의 법정동코드 전체자료(2026-11-15)에서 현존하는 시군구 256개를 마이그레이션(V14)으로 넣었다. 아래에 구가 있는 시(예: 수원시)는 카카오 위치 판정이 돌려주지 않으므로 넣지 않았다.
3. **`region_subdivisions` 와는 연결하지 않는다.** 일정의 지역과 병아리의 시군구는 서로 다른 목록이다.
4. **기존 병아리 행은 지웠다.** 아직 릴리스 전 기능이다.

## Consequences

- 행정구역이 개편되면 이 표를 새 마이그레이션으로 갱신해야 한다. 표에 없는 코드에서 쌓인 점령 시간은 어느 병아리의 경험치에도 들어가지 않는다.
- 클라이언트는 분양할 때 5자리 코드가 아니라 `legalDongCodeId` 를 보낸다. 목록은 `GET /api/fats/regions` 로 받는다.

## Related ADRs
- ADR 0008: Territory Occupation and Monthly Ranking
- ADR 0011: Fat Pre-adoption and EXP Sources
