# ADR 0012: Fat Region is a region_subdivisions Row, Linked to Occupation by Name

## Status
**Accepted** (2026-11-15). 같은 날 카카오 법정동 코드 표를 쓰는 안을 시도했다가 되돌렸다. 그 마이그레이션은 V12 하나로 정리해 남아 있지 않다.

## Context

펫은 전국 어느 시군구에서나 미리 분양받는다(ADR 0011). 경험치는 점령 시간에서 계산하는데, 점령 시간은 카카오가 위치마다 돌려주는 법정동 코드 앞 5자리로 쌓인다(ADR 0008).
시군구 목록은 이미 `region_subdivisions`(한국관광공사 법정동, LDongSeeder 가 채움)에 있다. 그러나 이 목록의 `region_code` 와 `code` 는 자릿수가 일정하지 않다. 그래서 둘을 이어 붙여도 카카오 코드가 되지 않는다. 예를 들어 세종특별자치시는 `region_code=36110`, `code=36110` 이다.
카카오 코드를 따로 관리하는 표도 두지 않기로 했다.

## Decision

1. **펫의 시군구는 `region_subdivisions` 의 한 행이다.** `fats.region_subdivision_id` 가 그 행을 가리킨다. 분양 요청은 `region_code` 와 `code` 를 각각 받아 두 컬럼으로 행을 찾는다.
2. **점령 시간과는 이름으로 연결한다.** 카카오가 위치를 판정할 때 준 시도·시군구 이름은 격자 캐시(`territory_region_cells.parent_name`, `region_name`)에 이미 저장되어 있다. 이 두 이름이 `regions.name`, `region_subdivisions.name` 과 같은 칸의 점령 시간이 그 펫의 경험치가 된다. 세종처럼 시군구가 없는 곳은 카카오도 시도 이름을 시군구 이름으로 쓰므로 같은 규칙으로 맞는다.
3. **아래에 구가 있는 시(예: 수원시)에서는 분양할 수 없다.** 카카오는 이런 시가 아니라 그 구(예: 수원시 장안구)를 돌려주므로 점령 시간이 쌓이지 않는다. 이런 시인지는 `region_subdivisions` 만으로 판단한다. 같은 시도 안에 이름이 "그 시 이름 + 공백"으로 시작하는 행이 있으면 아래에 구가 있는 시다.
4. **카카오 법정동 코드 표(`kakao_legal_dong_codes`)는 지웠고, 기존 펫 행도 지웠다.** 아직 릴리스 전 기능이다.

## Consequences

- 카카오와 한국관광공사의 이름이 다르면 그 시군구에서는 경험치가 쌓이지 않는다. 2026-11-15 개발 DB 기준으로는 256개 시군구의 이름이 모두 같았다. 행정구역 개편으로 한쪽 이름만 바뀌면 다시 확인해야 한다.
- 격자 캐시에 저장된 이름은 그 칸을 처음 판정한 때의 이름이다. 이름이 바뀌면 캐시도 새로 만들어야 한다.

## Related ADRs
- ADR 0008: Territory Occupation and Monthly Ranking
- ADR 0011: Fat Pre-adoption and EXP Sources
